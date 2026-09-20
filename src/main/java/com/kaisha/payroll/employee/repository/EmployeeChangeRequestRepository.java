package com.kaisha.payroll.employee.repository;

import com.kaisha.payroll.employee.entity.EmployeeChangeRequest;
import com.kaisha.payroll.employee.entity.EmployeeChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeChangeRequestRepository
        extends JpaRepository<EmployeeChangeRequest, Long> {

    List<EmployeeChangeRequest>
    findByCompany_CompanyIdAndStatusOrderByCreatedAtDesc(
            String companyId,
            EmployeeChangeRequestStatus status
    );

    Optional<EmployeeChangeRequest>
    findByRequestIdAndCompany_CompanyId(
            Long requestId,
            String companyId
    );
}