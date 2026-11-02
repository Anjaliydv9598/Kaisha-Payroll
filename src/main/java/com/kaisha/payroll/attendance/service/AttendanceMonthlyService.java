
package com.kaisha.payroll.attendance.service;

import com.kaisha.payroll.attendance.dto.AttendanceMonthlySummary;
import com.kaisha.payroll.attendance.entity.Attendance;
import com.kaisha.payroll.attendance.entity.AttendanceStatus;
import com.kaisha.payroll.attendance.repository.AttendanceRepository;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceMonthlyService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public AttendanceMonthlyService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            CurrentUserService currentUserService) {

        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
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

        for (Attendance attendance : records) {

            AttendanceStatus status = attendance.getStatus();

            if (status == null) {
                continue;
            }

            switch (status) {
                case PRESENT ->
                        summary.setPresentDays(summary.getPresentDays() + 1);

                case ABSENT ->
                        summary.setAbsentDays(summary.getAbsentDays() + 1);

                case HALF_DAY ->
                        summary.setHalfDays(summary.getHalfDays() + 1);

                case HOLIDAY ->
                        summary.setPaidHolidays(summary.getPaidHolidays() + 1);

                case WEEK_OFF ->
                        summary.setWeeklyOffs(summary.getWeeklyOffs() + 1);

                case MISSING_PUNCH ->
                        summary.setPendingMissingPunches(
                                summary.getPendingMissingPunches() + 1);

                case LEAVE -> {
                    // Leave classification will be integrated separately.
                }
            }
        }

        BigDecimal unpaidDays =
                BigDecimal.valueOf(summary.getAbsentDays())
                        .add(BigDecimal.valueOf(summary.getHalfDays())
                                .multiply(new BigDecimal("0.5")));

        summary.setUnpaidDays(unpaidDays);

        int scheduledWorkingDays =
                summary.getPresentDays()
                        + summary.getAbsentDays()
                        + summary.getHalfDays();

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
