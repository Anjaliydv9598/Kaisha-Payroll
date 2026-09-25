package com.kaisha.payroll.employee.numberseries.repository;

import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeNumberSeriesRepository
        extends JpaRepository<EmployeeNumberSeries, Long> {

    List<EmployeeNumberSeries>
    findByCompany_CompanyIdOrderByDepartmentAsc(
            String companyId
    );

    Optional<EmployeeNumberSeries>
    findByIdAndCompany_CompanyId(
            Long id,
            String companyId
    );

    Optional<EmployeeNumberSeries>
    findByDepartmentIgnoreCaseAndCompany_CompanyId(
            String department,
            String companyId
    );

    boolean
    existsByDepartmentIgnoreCaseAndCompany_CompanyId(
            String department,
            String companyId
    );

    boolean
    existsByDepartmentIgnoreCaseAndCompany_CompanyIdAndIdNot(
            String department,
            String companyId,
            Long id
    );
}