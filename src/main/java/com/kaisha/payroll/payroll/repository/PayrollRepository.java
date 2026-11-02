package com.kaisha.payroll.payroll.repository;

import com.kaisha.payroll.payroll.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByCompanyIdOrderByPayPeriodDesc(
            String companyId
    );

    List<Payroll> findByCompanyIdAndPayPeriodOrderByEmployeeIdAsc(
            String companyId,
            String payPeriod
    );

    List<Payroll> findByCompanyIdAndEmployeeIdOrderByPayPeriodDesc(
            String companyId,
            String employeeId
    );

    Optional<Payroll> findByPayrollIdAndCompanyId(
            Long payrollId,
            String companyId
    );

    Optional<Payroll> findByCompanyIdAndEmployeeIdAndPayPeriod(
            String companyId,
            String employeeId,
            String payPeriod
    );

    boolean existsByCompanyIdAndEmployeeIdAndPayPeriod(
            String companyId,
            String employeeId,
            String payPeriod
    );
}