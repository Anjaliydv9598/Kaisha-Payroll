package com.kaisha.payroll.payroll.service;

import com.kaisha.payroll.payroll.dto.PayrollResponse;
import com.kaisha.payroll.payroll.entity.Payroll;
import com.kaisha.payroll.payroll.repository.PayrollRepository;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.repository.SalaryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;

    private final SalaryRepository salaryRepository;

    public PayrollService(
            PayrollRepository payrollRepository,
            SalaryRepository salaryRepository) {

        this.payrollRepository =
                payrollRepository;

        this.salaryRepository =
                salaryRepository;
    }

    // =====================================================
    // PROCESS MONTH
    // =====================================================

    public List<PayrollResponse>
    processPayroll(
            String payPeriod) {

        if (payPeriod == null ||
                !payPeriod.matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Pay period must be YYYY-MM"
            );
        }

        List<Salary> salaries =
                salaryRepository
                        .findByPayPeriodOrderByEmployeeIdAsc(
                                payPeriod
                        );

        if (salaries.isEmpty()) {

            throw new RuntimeException(
                    "No salary records found for "
                            + payPeriod
            );
        }

        for (Salary salary : salaries) {

            Payroll payroll =
                    payrollRepository
                            .findByEmployeeIdAndPayPeriod(
                                    salary.getEmployeeId(),
                                    payPeriod
                            )
                            .orElseGet(
                                    Payroll::new
                            );

            payroll.setEmployeeId(
                    salary.getEmployeeId()
            );

            payroll.setPayPeriod(
                    payPeriod
            );

            payroll.setGrossSalary(
                    salary.getGrossSalary()
            );

            payroll.setTotalDeductions(
                    salary.getTotalDeductions()
            );

            payroll.setNetSalary(
                    salary.getNetSalary()
            );

            payroll.setStatus(
                    Payroll.PayrollStatus.PROCESSED
            );

            payroll.setProcessedAt(
                    LocalDateTime.now()
            );

            payrollRepository.save(
                    payroll
            );
        }

        return getByPeriod(
                payPeriod
        );
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse>
    getAll() {

        return payrollRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET PERIOD
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse>
    getByPeriod(
            String payPeriod) {

        return payrollRepository
                .findByPayPeriodOrderByEmployeeIdAsc(
                        payPeriod
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET ID
    // =====================================================

    @Transactional(readOnly = true)
    public PayrollResponse
    getById(Long payrollId) {

        Payroll payroll =
                payrollRepository.findById(
                        payrollId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Payroll record not found"
                        )
                );

        return toResponse(
                payroll
        );
    }

    // =====================================================
    // GET EMPLOYEE
    // =====================================================

    @Transactional(readOnly = true)
    public List<PayrollResponse>
    getByEmployee(
            String employeeId) {

        return payrollRepository
                .findByEmployeeIdOrderByPayPeriodDesc(
                        employeeId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // CANCEL
    // =====================================================

    public PayrollResponse
    cancel(Long payrollId) {

        Payroll payroll =
                payrollRepository.findById(
                        payrollId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Payroll record not found"
                        )
                );

        payroll.setStatus(
                Payroll.PayrollStatus.CANCELLED
        );

        return toResponse(
                payrollRepository.save(
                        payroll
                )
        );
    }

    // =====================================================
    // RESPONSE
    // =====================================================

    private PayrollResponse toResponse(
            Payroll payroll) {

        PayrollResponse response =
                new PayrollResponse();

        response.setPayrollId(
                payroll.getPayrollId()
        );

        response.setEmployeeId(
                payroll.getEmployeeId()
        );

        response.setPayPeriod(
                payroll.getPayPeriod()
        );

        response.setGrossSalary(
                payroll.getGrossSalary()
        );

        response.setTotalDeductions(
                payroll.getTotalDeductions()
        );

        response.setNetSalary(
                payroll.getNetSalary()
        );

        response.setStatus(
                payroll.getStatus()
        );

        response.setProcessedAt(
                payroll.getProcessedAt()
        );

        response.setCreatedAt(
                payroll.getCreatedAt()
        );

        return response;
    }
}