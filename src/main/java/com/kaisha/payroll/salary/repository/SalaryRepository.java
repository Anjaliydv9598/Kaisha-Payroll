package com.kaisha.payroll.salary.repository;

import com.kaisha.payroll.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository
        extends JpaRepository<Salary, Long> {

    List<Salary>
    findByPayPeriodOrderByEmployeeIdAsc(
            String payPeriod
    );

    List<Salary>
    findByEmployeeIdOrderByPayPeriodDesc(
            String employeeId
    );

    Optional<Salary>
    findByEmployeeIdAndPayPeriod(
            String employeeId,
            String payPeriod
    );

    boolean
    existsByEmployeeIdAndPayPeriod(
            String employeeId,
            String payPeriod
    );

    boolean
    existsByEmployeeIdAndPayPeriodAndSalaryIdNot(
            String employeeId,
            String payPeriod,
            Long salaryId
    );
}