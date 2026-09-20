package com.kaisha.payroll.employee.repository;

import com.kaisha.payroll.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee>
    findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
            String companyId
    );

    List<Employee>
    findByCompany_CompanyIdOrderByEmployeeIdAsc(
            String companyId
    );

    Optional<Employee>
    findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
            String employeeId,
            String companyId
    );

    Optional<Employee>
    findByEmployeeIdAndCompany_CompanyId(
            String employeeId,
            String companyId
    );

    boolean
    existsByEmployeeIdAndCompany_CompanyId(
            String employeeId,
            String companyId
    );
}