
package com.kaisha.payroll.leave.controller;

import com.kaisha.payroll.leave.dto.LeaveDecisionRequest;
import com.kaisha.payroll.leave.dto.LeaveRequest;
import com.kaisha.payroll.leave.dto.LeaveResponse;
import com.kaisha.payroll.leave.service.LeaveService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Employee submits a leave request
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public LeaveResponse applyLeave(@RequestBody LeaveRequest request) {
        return leaveService.applyLeave(request);
    }

    // Admin views all company leave requests
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveResponse> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    // Admin views pending requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveResponse> getPendingLeaves() {
        return leaveService.getPendingLeaves();
    }

    // Employee views leave requests for an employee ID
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public List<LeaveResponse> getEmployeeLeaves(
            @PathVariable String employeeId
    ) {
        return leaveService.getEmployeeLeaves(employeeId);
    }

    // Admin approves or rejects a request
    @PutMapping("/{leaveId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveResponse decideLeave(
            @PathVariable Long leaveId,
            @RequestBody LeaveDecisionRequest request
    ) {
        return leaveService.decideLeave(leaveId, request);
    }
}
