package com.kaisha.payroll.department.repository;

import com.kaisha.payroll.department.entity.DepartmentChangeRequest;
import com.kaisha.payroll.department.entity.DepartmentChangeRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentChangeRequestRepository
        extends JpaRepository<DepartmentChangeRequest, Long> {

    List<DepartmentChangeRequest>
    findByCompany_CompanyIdAndStatusOrderByCreatedAtDesc(
            String companyId,
            DepartmentChangeRequestStatus status
    );

    Optional<DepartmentChangeRequest>
    findByRequestIdAndCompany_CompanyId(
            Long requestId,
            String companyId
    );
}