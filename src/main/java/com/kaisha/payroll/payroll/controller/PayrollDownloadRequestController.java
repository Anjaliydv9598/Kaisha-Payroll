package com.kaisha.payroll.payroll.controller;

import com.kaisha.payroll.payroll.dto.PayrollDownloadRequestDto;
import com.kaisha.payroll.payroll.entity.PayrollDownloadRequest;
import com.kaisha.payroll.payroll.service.PayrollDownloadRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/payroll/download-requests")
public class PayrollDownloadRequestController {

    private final PayrollDownloadRequestService service;

    public PayrollDownloadRequestController(
            PayrollDownloadRequestService service
    ) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PayrollDownloadRequest> create(
            @Valid @RequestBody PayrollDownloadRequestDto dto,
            Principal principal
    ) {

        String username =
                principal != null
                        ? principal.getName()
                        : "UNKNOWN";

        return ResponseEntity.ok(
                service.createRequest(
                        dto,
                        username
                )
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PayrollDownloadRequest>>
    getPending() {

        return ResponseEntity.ok(
                service.getPending()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<List<PayrollDownloadRequest>>
    getMine(
            Principal principal
    ) {

        String username =
                principal != null
                        ? principal.getName()
                        : "UNKNOWN";

        return ResponseEntity.ok(
                service.getByUser(username)
        );
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<PayrollDownloadRequest> approve(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                service.approve(requestId)
        );
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<PayrollDownloadRequest> reject(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                service.reject(requestId)
        );
    }
}