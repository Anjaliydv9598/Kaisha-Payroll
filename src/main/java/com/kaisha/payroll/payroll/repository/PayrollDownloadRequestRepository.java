package com.kaisha.payroll.payroll.repo;

import com.kaisha.payroll.payroll.entity.PayrollDownloadRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollDownloadRequestRepository
        extends JpaRepository<PayrollDownloadRequest, Long> {

    List<PayrollDownloadRequest>
    findByStatusOrderByCreatedAtDesc(String status);

    List<PayrollDownloadRequest>
    findByRequestedByOrderByCreatedAtDesc(
            String requestedBy
    );
}