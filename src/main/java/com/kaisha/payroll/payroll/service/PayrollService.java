
package com.kaisha.payroll.payroll.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.attendance.dto.AttendanceMonthlySummary;
import com.kaisha.payroll.attendance.service.AttendanceMonthlyService;
import com.kaisha.payroll.payroll.dto.PayrollResponse;
import com.kaisha.payroll.payroll.entity.Payroll;
import com.kaisha.payroll.payroll.repository.PayrollRepository;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.repository.SalaryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final SalaryRepository salaryRepository;
    private final CurrentUserService currentUserService;
    private final AttendanceMonthlyService attendanceMonthlyService;

    public PayrollService(
            PayrollRepository payrollRepository,
            SalaryRepository salaryRepository,
            CurrentUserService currentUserService,
            AttendanceMonthlyService attendanceMonthlyService) {

        this.payrollRepository = payrollRepository;
        this.salaryRepository = salaryRepository;
        this.currentUserService = currentUserService;
        this.attendanceMonthlyService = attendanceMonthlyService;
    }

    private String getCompanyId() {
        return currentUserService.getCurrentCompanyId();
    }

    // =====================================================
    // PROCESS PAYROLL WITH ATTENDANCE
    // =====================================================

    public List<PayrollResponse> processPayroll(String payPeriod) {

        validatePayPeriod(payPeriod);

        String companyId = getCompanyId();

        int year = Integer.parseInt(payPeriod.substring(0, 4));
        int month = Integer.parseInt(payPeriod.substring(5, 7));

        List<Salary> salaries =
                salaryRepository
                        .findByCompanyIdAndPayPeriodOrderByEmployeeIdAsc(
                                companyId,
                                payPeriod
                        );

        if (salaries.isEmpty()) {
            throw new RuntimeException(
                    "No salary records found for " + payPeriod
            );
        }

        for (Salary salary : salaries) {

            Payroll payroll =
                    payrollRepository
                            .findByCompanyIdAndEmployeeIdAndPayPeriod(
                                    companyId,
                                    salary.getEmployeeId(),
                                    payPeriod
                            )
                            .orElseGet(Payroll::new);

            if (payroll.getStatus() == Payroll.PayrollStatus.CANCELLED) {
                throw new RuntimeException(
                        "Payroll was cancelled for employee "
                                + salary.getEmployeeId()
                                + " in " + payPeriod
                );
            }

            BigDecimal grossSalary = salary.getGrossSalary();

            if (grossSalary == null || grossSalary.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Invalid gross salary for employee "
                                + salary.getEmployeeId()
                );
            }

            BigDecimal salaryDeductions =
                    salary.getTotalDeductions() == null
                            ? BigDecimal.ZERO
                            : salary.getTotalDeductions();

            // Get employee's attendance summary.
            AttendanceMonthlySummary attendance =
                    attendanceMonthlyService.getMonthlySummary(
                            salary.getEmployeeId(),
                            year,
                            month,
                            grossSalary
                    );

            BigDecimal attendanceDeduction =
                    attendance.getAttendanceDeduction() == null
                            ? BigDecimal.ZERO
                            : attendance.getAttendanceDeduction();

            // Existing salary deductions + attendance deductions.
            BigDecimal totalDeductions =
                    salaryDeductions.add(attendanceDeduction)
                            .setScale(2, RoundingMode.HALF_UP);

            BigDecimal netSalary =
                    grossSalary.subtract(totalDeductions)
                            .setScale(2, RoundingMode.HALF_UP);

            if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "Total deductions exceed gross salary for employee "
                                + salary.getEmployeeId()
                );
            }

            // Save payroll information.
            payroll.setCompanyId(companyId);
            payroll.setEmployeeId(salary.getEmployeeId());
            payroll.setPayPeriod(payPeriod);

            payroll.setGrossSalary(grossSalary);
            payroll.setAttendanceDeduction(attendanceDeduction);
            payroll.setOtherDeductions(salaryDeductions);
            payroll.setTotalDeductions(totalDeductions);
            payroll.setNetSalary(netSalary);

            // Attendance snapshot.
            payroll.setPresentDays(attendance.getPresentDays());
            payroll.setAbsentDays(attendance.getAbsentDays());
            payroll.setHalfDays(attendance.getHalfDays());
            payroll.setPaidHolidays(attendance.getPaidHolidays());
            payroll.setWeeklyOffs(attendance.getWeeklyOffs());
            payroll.setPaidLeaveDays(attendance.getPaidLeaveDays());
            payroll.setUnpaidLeaveDays(attendance.getUnpaidLeaveDays());

            payroll.setStatus(Payroll.PayrollStatus.PROCESSED);
            payroll.setProcessedAt(LocalDateTime.now());

            payrollRepository.save(payroll);
        }

        return getByPeriod(payPeriod);
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse> getAll() {

        String companyId = getCompanyId();

        return payrollRepository
                .findByCompanyIdOrderByPayPeriodDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY PERIOD
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse> getByPeriod(String payPeriod) {

        validatePayPeriod(payPeriod);

        String companyId = getCompanyId();

        return payrollRepository
                .findByCompanyIdAndPayPeriodOrderByEmployeeIdAsc(
                        companyId,
                        payPeriod
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public PayrollResponse getById(Long payrollId) {

        String companyId = getCompanyId();

        Payroll payroll = findPayrollForCompany(
                payrollId,
                companyId
        );

        return toResponse(payroll);
    }

    // =====================================================
    // GET BY EMPLOYEE
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse> getByEmployee(String employeeId) {

        if (employeeId == null || employeeId.isBlank()) {
            throw new RuntimeException("Employee ID is required");
        }

        String companyId = getCompanyId();

        return payrollRepository
                .findByCompanyIdAndEmployeeIdOrderByPayPeriodDesc(
                        companyId,
                        employeeId.trim()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // CANCEL
    // =====================================================

    public PayrollResponse cancel(Long payrollId) {

        String companyId = getCompanyId();

        Payroll payroll = findPayrollForCompany(
                payrollId,
                companyId
        );

        if (payroll.getStatus() == Payroll.PayrollStatus.CANCELLED) {
            throw new RuntimeException("Payroll is already cancelled");
        }

        payroll.setStatus(Payroll.PayrollStatus.CANCELLED);

        return toResponse(payrollRepository.save(payroll));
    }

    // =====================================================
    // FIND PAYROLL WITH COMPANY CHECK
    // =====================================================

    private Payroll findPayrollForCompany(
            Long payrollId,
            String companyId) {

        if (payrollId == null) {
            throw new RuntimeException("Payroll ID is required");
        }

        return payrollRepository
                .findByPayrollIdAndCompanyId(payrollId, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Payroll record not found")
                );
    }

    // =====================================================
    // VALIDATE PERIOD
    // =====================================================

    private void validatePayPeriod(String payPeriod) {

        if (payPeriod == null ||
                !payPeriod.matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Pay period must be YYYY-MM"
            );
        }

        int month = Integer.parseInt(payPeriod.substring(5, 7));

        if (month < 1 || month > 12) {
            throw new RuntimeException(
                    "Pay period month must be between 01 and 12"
            );
        }
    }

    // =====================================================
    // RESPONSE MAPPING
    // =====================================================

    private PayrollResponse toResponse(Payroll payroll) {

        PayrollResponse response = new PayrollResponse();

        response.setPayrollId(payroll.getPayrollId());
        response.setEmployeeId(payroll.getEmployeeId());
        response.setPayPeriod(payroll.getPayPeriod());

        response.setGrossSalary(payroll.getGrossSalary());
        response.setTotalDeductions(payroll.getTotalDeductions());
        response.setNetSalary(payroll.getNetSalary());

        response.setStatus(payroll.getStatus());
        response.setProcessedAt(payroll.getProcessedAt());
        response.setCreatedAt(payroll.getCreatedAt());

        response.setAttendanceDeduction(payroll.getAttendanceDeduction());
        response.setOtherDeductions(payroll.getOtherDeductions());

        response.setPresentDays(payroll.getPresentDays());
        response.setAbsentDays(payroll.getAbsentDays());
        response.setHalfDays(payroll.getHalfDays());
        response.setPaidHolidays(payroll.getPaidHolidays());
        response.setWeeklyOffs(payroll.getWeeklyOffs());
        response.setPaidLeaveDays(payroll.getPaidLeaveDays());
        response.setUnpaidLeaveDays(payroll.getUnpaidLeaveDays());

        return response;
    }
}
