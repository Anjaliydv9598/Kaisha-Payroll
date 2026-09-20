package com.kaisha.payroll.employee.controller;

import com.kaisha.payroll.employee.service.EmployeeChangeRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employee-change-requests")
@CrossOrigin
public class EmployeeChangeRequestController {

    private final EmployeeChangeRequestService requestService;

    public EmployeeChangeRequestController(
            EmployeeChangeRequestService requestService
    ) {
        this.requestService = requestService;
    }

    // ============================================================
    // STAFF CREATE REQUEST
    // ============================================================

    @PostMapping("/{employeeId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> createRequest(
            @PathVariable String employeeId,
            @RequestBody Map<String, String> request
    ) {

        return ResponseEntity.ok(
                requestService.createRequest(
                        employeeId,
                        request
                )
        );
    }

    // ============================================================
    // ADMIN GET PENDING
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRequests() {

        return ResponseEntity.ok(
                requestService.getPendingRequests()
        );
    }

    // ============================================================
    // ADMIN APPROVE
    // ============================================================

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                requestService.approveRequest(
                        requestId
                )
        );
    }

    // ============================================================
    // ADMIN REJECT
    // ============================================================

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @PathVariable Long requestId
    ) {

        return ResponseEntity.ok(
                requestService.rejectRequest(
                        requestId
                )
        );
    }
}