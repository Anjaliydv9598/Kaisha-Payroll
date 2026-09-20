package com.kaisha.payroll.payroll.service;

import com.kaisha.payroll.payroll.dto.PayrollDownloadRequestDto;
import com.kaisha.payroll.payroll.entity.PayrollDownloadRequest;
import com.kaisha.payroll.payroll.repo.PayrollDownloadRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PayrollDownloadRequestService {

    private final PayrollDownloadRequestRepository repository;

    public PayrollDownloadRequestService(
            PayrollDownloadRequestRepository repository
    ) {
        this.repository = repository;
    }

    // =========================
    // CREATE REQUEST
    // =========================

    public PayrollDownloadRequest createRequest(
            PayrollDownloadRequestDto dto,
            String requestedBy
    ) {

        validateFormat(dto.getFormat());

        PayrollDownloadRequest request =
                new PayrollDownloadRequest();

        request.setRequestedBy(requestedBy);
        request.setPayPeriod(dto.getPayPeriod());
        request.setScope(dto.getScope());

        request.setEmployeeIds(
                dto.getEmployeeIds()
        );

        request.setFromRecord(
                dto.getFromRecord()
        );

        request.setToRecord(
                dto.getToRecord()
        );

        request.setFormat(
                dto.getFormat().trim().toUpperCase()
        );

        request.setStatus("PENDING");

        return repository.save(request);
    }

    // =========================
    // GET PENDING
    // =========================

    @Transactional(readOnly = true)
    public List<PayrollDownloadRequest> getPending() {

        return repository
                .findByStatusOrderByCreatedAtDesc(
                        "PENDING"
                );
    }

    // =========================
    // GET USER REQUESTS
    // =========================

    @Transactional(readOnly = true)
    public List<PayrollDownloadRequest> getByUser(
            String username
    ) {

        return repository
                .findByRequestedByOrderByCreatedAtDesc(
                        username
                );
    }

    // =========================
    // APPROVE
    // =========================

    public PayrollDownloadRequest approve(
            Long requestId
    ) {

        PayrollDownloadRequest request =
                getRequest(requestId);

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException(
                    "Only PENDING requests can be approved"
            );
        }

        request.setStatus("APPROVED");
        request.setApprovedAt(
                LocalDateTime.now()
        );

        return repository.save(request);
    }

    // =========================
    // REJECT
    // =========================

    public PayrollDownloadRequest reject(
            Long requestId
    ) {

        PayrollDownloadRequest request =
                getRequest(requestId);

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException(
                    "Only PENDING requests can be rejected"
            );
        }

        request.setStatus("REJECTED");
        request.setRejectedAt(
                LocalDateTime.now()
        );

        return repository.save(request);
    }

    // =========================
    // FIND
    // =========================

    @Transactional(readOnly = true)
    public PayrollDownloadRequest getRequest(
            Long requestId
    ) {

        return repository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Download request not found: "
                                        + requestId
                        )
                );
    }

    private void validateFormat(
            String format
    ) {

        if (format == null) {
            throw new IllegalArgumentException(
                    "Download format is required"
            );
        }

        String normalized =
                format.trim().toUpperCase();

        if (!normalized.equals("PDF")
                && !normalized.equals("EXCEL")
                && !normalized.equals("CSV")) {

            throw new IllegalArgumentException(
                    "Format must be PDF, EXCEL or CSV"
            );
        }
    }
}