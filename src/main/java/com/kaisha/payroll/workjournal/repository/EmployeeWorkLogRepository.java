
package com.kaisha.payroll.workjournal.repository;

import com.kaisha.payroll.workjournal.entity.EmployeeWorkLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeWorkLogRepository
        extends JpaRepository<EmployeeWorkLog, Long> {

    // All work entries of one employee
    List<EmployeeWorkLog>
    findByEmployee_IdAndCompany_CompanyIdOrderByWorkDateDescCreatedAtDesc(
            Long employeeId,
            String companyId
    );

    // Find one entry belonging to an employee and company
    Optional<EmployeeWorkLog>
    findByIdAndEmployee_IdAndCompany_CompanyId(
            Long id,
            Long employeeId,
            String companyId
    );

    // Highlight entries of one employee
    List<EmployeeWorkLog>
    findByEmployee_IdAndCompany_CompanyIdAndHighlightTrueOrderByWorkDateDesc(
            Long employeeId,
            String companyId
    );

    // Entries within a date range
    List<EmployeeWorkLog>
    findByEmployee_IdAndCompany_CompanyIdAndWorkDateBetweenOrderByWorkDateDesc(
            Long employeeId,
            String companyId,
            LocalDate from,
            LocalDate to
    );
}
