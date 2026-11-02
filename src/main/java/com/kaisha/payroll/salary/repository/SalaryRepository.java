package com.kaisha.payroll.salary.repository;

import com.kaisha.payroll.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByCompanyIdAndPayPeriodOrderByEmployeeIdAsc(
            String companyId,
            String payPeriod
    );

    List<Salary> findByCompanyIdOrderByPayPeriodDesc(
            String companyId
    );

    List<Salary> findByCompanyIdAndEmployeeIdOrderByPayPeriodDesc(
            String companyId,
            String employeeId
    );


    Optional<Salary> findBySalaryIdAndCompanyId(
            Long salaryId,
            String companyId
    );

    Optional<Salary> findByCompanyIdAndEmployeeIdAndPayPeriod(
            String companyId,
            String employeeId,
            String payPeriod
    );

    boolean existsByCompanyIdAndEmployeeIdAndPayPeriod(
            String companyId,
            String employeeId,
            String payPeriod
    );

    boolean existsByCompanyIdAndEmployeeIdAndPayPeriodAndSalaryIdNot(
            String companyId,
            String employeeId,
            String payPeriod,
            Long salaryId
    );
}