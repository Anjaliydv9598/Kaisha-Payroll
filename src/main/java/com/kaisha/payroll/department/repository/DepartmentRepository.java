package com.kaisha.payroll.department.repository;

import com.kaisha.payroll.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department>
    findByCompany_CompanyIdAndActiveTrueOrderByDepartmentNameAsc(
            String companyId
    );

    Optional<Department>
    findByDepartmentIdAndCompany_CompanyIdAndActiveTrue(
            Long departmentId,
            String companyId
    );

    boolean
    existsByCompany_CompanyIdAndDepartmentNameIgnoreCaseAndActiveTrue(
            String companyId,
            String departmentName
    );

    boolean
    existsByCompany_CompanyIdAndDepartmentNameIgnoreCaseAndActiveTrueAndDepartmentIdNot(
            String companyId,
            String departmentName,
            Long departmentId
    );
}