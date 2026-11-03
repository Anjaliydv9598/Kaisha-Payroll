
package com.kaisha.payroll.employee.repository;

import com.kaisha.payroll.employee.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    // Get active employees of a company
    List<Employee> findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
            String companyId
    );

    // Get all employees of a company
    List<Employee> findByCompany_CompanyIdOrderByEmployeeIdAsc(
            String companyId
    );

    // Get employees whose details are completed and active
    List<Employee>
    findByCompany_CompanyIdAndDetailsCompletedTrueAndActiveTrueOrderByEmployeeIdAsc(
            String companyId
    );

    // Get employees whose details are incomplete
    List<Employee>
    findByCompany_CompanyIdAndDetailsCompletedFalseOrderByEmployeeIdAsc(
            String companyId
    );

    // Find active employee using Employee ID and Company ID
    Optional<Employee> findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
            String employeeId,
            String companyId
    );

    // Find employee using Employee ID and Company ID
    Optional<Employee> findByEmployeeIdAndCompany_CompanyId(
            String employeeId,
            String companyId
    );

    // Check whether Employee ID already exists in company
    boolean existsByEmployeeIdAndCompany_CompanyId(
            String employeeId,
            String companyId
    );
}
