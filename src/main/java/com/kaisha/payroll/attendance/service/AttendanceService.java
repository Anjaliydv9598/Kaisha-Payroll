
package com.kaisha.payroll.attendance.service;

import com.kaisha.payroll.attendance.dto.AttendanceMonthlySummary;
import com.kaisha.payroll.attendance.entity.Attendance;
import com.kaisha.payroll.attendance.entity.AttendanceStatus;
import com.kaisha.payroll.attendance.repository.AttendanceRepository;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import com.kaisha.payroll.leave.entity.Leave;
import com.kaisha.payroll.leave.entity.LeaveStatus;
import com.kaisha.payroll.leave.entity.LeaveType;
import com.kaisha.payroll.leave.repository.LeaveRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AttendanceMonthlyService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final CurrentUserService currentUserService;

    public AttendanceMonthlyService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            LeaveRepository leaveRepository,
            CurrentUserService currentUserService) {

        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlySummary getMonthlySummary(
            String employeeId,
            int year,
            int month,
            BigDecimal monthlyGrossSalary) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }

        if (monthlyGrossSalary == null
                || monthlyGrossSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid monthly salary");
        }

        String companyId = currentUserService.getCurrentCompanyId();

        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        employeeId, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found in your company"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(
                startDate.lengthOfMonth());

        List<Attendance> records =
                attendanceRepository.findByEmployeeAndAttendanceDateBetween(
                        employee, startDate, endDate);

        AttendanceMonthlySummary summary =
                new AttendanceMonthlySummary();

        summary.setEmployeeId(employeeId);
        summary.setYear(year);
        summary.setMonth(month);

        // Store attendance by date for leave-date checking.
        Map<LocalDate, Attendance> attendanceByDate = new HashMap<>();

        for (Attendance attendance : records) {
            attendanceByDate.put(
                    attendance.getAttendanceDate(), attendance);
        }

        // Fetch approved leaves overlapping this month.
        List<Leave> approvedLeaves =
                leaveRepository
                        .findByCompanyIdAndEmployee_EmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                companyId,
                                employeeId,
                                LeaveStatus.APPROVED,
                                endDate,
                                startDate
                        );

        Set<LocalDate> appliedLeaveDates = new HashSet<>();

        int paidLeaveDays = 0;
        int unpaidLeaveDays = 0;

        // Count approved leave dates.
        for (Leave leave : approvedLeaves) {

            LocalDate leaveStart = leave.getStartDate().isBefore(startDate)
                    ? startDate : leave.getStartDate();

            LocalDate leaveEnd = leave.getEndDate().isAfter(endDate)
                    ? endDate : leave.getEndDate();

            for (LocalDate date = leaveStart;
                 !date.isAfter(leaveEnd);
                 date = date.plusDays(1)) {

                Attendance attendance = attendanceByDate.get(date);

                // Do not consume leave on a holiday, weekly off,
                // or a day when the employee actually attended.
                if (attendance != null) {

                    AttendanceStatus status = attendance.getStatus();

                    if (status == AttendanceStatus.HOLIDAY
                            || status == AttendanceStatus.WEEK_OFF
                            || status == AttendanceStatus.PRESENT) {
                        continue;
                    }
                }

                // Prevent duplicate counting of the same date.
                if (!appliedLeaveDates.add(date)) {
                    continue;
                }

                if (leave.getLeaveType() == LeaveType.PAID) {
                    paidLeaveDays++;
                } else if (leave.getLeaveType() == LeaveType.UNPAID) {
                    unpaidLeaveDays++;
                }
            }
        }

        summary.setPaidLeaveDays(paidLeaveDays);
        summary.setUnpaidLeaveDays(unpaidLeaveDays);

        // Count attendance records, excluding dates already
        // classified as approved leave.
        for (Attendance attendance : records) {

            LocalDate attendanceDate = attendance.getAttendanceDate();

            if (appliedLeaveDates.contains(attendanceDate)) {
                continue;
            }

            AttendanceStatus status = attendance.getStatus();

            if (status == null) {
                continue;
            }

            switch (status) {

                case PRESENT ->
                        summary.setPresentDays(
                                summary.getPresentDays() + 1);

                case ABSENT ->
                        summary.setAbsentDays(
                                summary.getAbsentDays() + 1);

                case HALF_DAY ->
                        summary.setHalfDays(
                                summary.getHalfDays() + 1);

                case HOLIDAY ->
                        summary.setPaidHolidays(
                                summary.getPaidHolidays() + 1);

                case WEEK_OFF ->
                        summary.setWeeklyOffs(
                                summary.getWeeklyOffs() + 1);

                case MISSING_PUNCH ->
                        summary.setPendingMissingPunches(
                                summary.getPendingMissingPunches() + 1);

                case LEAVE -> {
                    // Only approved leave requests are counted above.
                }
            }
        }

        // Unpaid days = absent + half-day + approved unpaid leave.
        BigDecimal unpaidDays =
                BigDecimal.valueOf(summary.getAbsentDays())
                        .add(
                                BigDecimal.valueOf(summary.getHalfDays())
                                        .multiply(new BigDecimal("0.5"))
                        )
                        .add(BigDecimal.valueOf(unpaidLeaveDays));

        summary.setUnpaidDays(unpaidDays);

        // Include approved paid and unpaid leave in scheduled days.
        int scheduledWorkingDays =
                summary.getPresentDays()
                        + summary.getAbsentDays()
                        + summary.getHalfDays()
                        + paidLeaveDays
                        + unpaidLeaveDays;

        summary.setScheduledWorkingDays(scheduledWorkingDays);

        if (summary.getPendingMissingPunches() > 0) {
            throw new IllegalStateException(
                    "Payroll cannot be processed. "
                            + "Pending missing punches require admin review.");
        }

        if (scheduledWorkingDays > 0) {

            BigDecimal dailyRate =
                    monthlyGrossSalary.divide(
                            BigDecimal.valueOf(scheduledWorkingDays),
                            2,
                            RoundingMode.HALF_UP);

            BigDecimal deduction =
                    dailyRate.multiply(unpaidDays)
                            .setScale(2, RoundingMode.HALF_UP);

            summary.setDailyRate(dailyRate);
            summary.setAttendanceDeduction(deduction);
        }

        return summary;
    }
}
