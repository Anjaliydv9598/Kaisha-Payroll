package com.kaisha.payroll.department.controller;

import com.kaisha.payroll.department.dto.DepartmentChangeRequestDto;
import com.kaisha.payroll.department.service.DepartmentChangeRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department-requests")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartmentChangeRequestController {

    private final DepartmentChangeRequestService requestService;

    public DepartmentChangeRequestController(
            DepartmentChangeRequestService requestService) {

        this.requestService = requestService;
    }

    /*
     * STAFF creates request.
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> createRequest(
            @RequestBody DepartmentChangeRequestDto request) {

        try {

            return ResponseEntity.ok(
                    requestService.createRequest(request)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /*
     * ADMIN views pending requests.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRequests() {

        try {

            return ResponseEntity.ok(
                    requestService.getPendingRequests()
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /*
     * ADMIN accepts.
     */
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long requestId) {

        try {

            return ResponseEntity.ok(
                    requestService.approveRequest(
                            requestId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /*
     * ADMIN rejects.
     */
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(
            @PathVariable Long requestId) {

        try {

            return ResponseEntity.ok(
                    requestService.rejectRequest(
                            requestId
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}