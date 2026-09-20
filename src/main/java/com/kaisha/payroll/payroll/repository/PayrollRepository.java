package com.kaisha.payroll.payroll.repository;

import com.kaisha.payroll.payroll.entity.Payroll;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository
        extends JpaRepository<Payroll, Long> {

    List<Payroll>
    findByPayPeriodOrderByEmployeeIdAsc(
            String payPeriod
    );

    List<Payroll>
    findByEmployeeIdOrderByPayPeriodDesc(
            String employeeId
    );

    Optional<Payroll>
    findByEmployeeIdAndPayPeriod(
            String employeeId,
            String payPeriod
    );
}