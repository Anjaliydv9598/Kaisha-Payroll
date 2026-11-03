package com.kaisha.payroll.auth.controller;

import com.kaisha.payroll.auth.dto.CreateStaffRequest;
import com.kaisha.payroll.auth.dto.StaffEmployeeResponse;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.auth.service.StaffEmployeeLinkService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/staff")
public class StaffEmployeeLinkController {

    private final StaffEmployeeLinkService staffService;
    private final UserRepository userRepository;

    public StaffEmployeeLinkController(
            StaffEmployeeLinkService staffService,
            UserRepository userRepository
    ) {
        this.staffService = staffService;
        this.userRepository = userRepository;
    }

    private User getAdmin(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        User admin = userRepository
                .findByUsernameWithCompany(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (admin.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN can manage Staff Login"
            );
        }

        if (admin.getCompany() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin is not linked to a company"
            );
        }

        return admin;
    }

    // GET: Existing employees and login status
    @GetMapping("/employees")
    public ResponseEntity<List<StaffEmployeeResponse>> getEmployees(
            Authentication authentication
    ) {
        User admin = getAdmin(authentication);

        String companyId = admin.getCompany().getCompanyId();

        return ResponseEntity.ok(
                staffService.getEmployees(companyId)
        );
    }

    // POST: Create staff login
    @PostMapping
    public ResponseEntity<String> createStaff(
            @RequestBody CreateStaffRequest request,
            Authentication authentication
    ) {
        User admin = getAdmin(authentication);

        String companyId = admin.getCompany().getCompanyId();

        staffService.createStaffLogin(request, companyId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Staff login created successfully");
    }

    // GET: View complete employee details
    @GetMapping("/employees/{employeeDbId}/details")
    public ResponseEntity<Map<String, Object>> getEmployeeDetails(
            @PathVariable Long employeeDbId,
            Authentication authentication
    ) {
        User admin = getAdmin(authentication);

        String companyId = admin.getCompany().getCompanyId();

        return ResponseEntity.ok(
                staffService.getEmployeeDetails(employeeDbId, companyId)
        );
    }
}