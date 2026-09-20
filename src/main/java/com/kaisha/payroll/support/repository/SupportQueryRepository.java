package com.kaisha.payroll.support.repository;

import com.kaisha.payroll.support.entity.SupportQuery;
import com.kaisha.payroll.support.entity.SupportQueryStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportQueryRepository extends JpaRepository<SupportQuery, Long> {

    List<SupportQuery> findAllByOrderByCreatedAtDesc();

    List<SupportQuery> findByCompanyIdOrderByCreatedAtDesc(
            String companyId
    );

    List<SupportQuery> findByStatusOrderByCreatedAtDesc(
            SupportQueryStatus status
    );

    long countByStatus(SupportQueryStatus status);
}