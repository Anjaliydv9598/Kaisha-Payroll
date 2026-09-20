package com.kaisha.payroll.company.controller;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.company.dto.CompanyRequest;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.service.CompanyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyController {

    private final CompanyService companyService;
    private final UserRepository userRepository;

    public CompanyController(
            CompanyService companyService,
            UserRepository userRepository) {

        this.companyService = companyService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // GET COMPANY
    // ADMIN + STAFF
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getCompany(
            org.springframework.security.core.Authentication authentication) {

        try {

            User user = getLoggedInUser(authentication);

            Company company =
                    companyService.getCompanyForUser(user);

            /*
             * STAFF:
             * Only company name is returned.
             */

            if ("STAFF".equals(user.getRole().name())) {

                return ResponseEntity.ok(
                        new StaffCompanyResponse(
                                company.getCompanyName()
                        )
                );
            }

            /*
             * ADMIN:
             * Complete company information.
             */

            return ResponseEntity.ok(company);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new ErrorResponse(
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE COMPANY
    // ADMIN ONLY
    //
    // Used for:
    // Company Name
    // Address / Location
    // PAN
    // TAN
    // Telephone
    // =========================================================

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCompany(
            @RequestBody CompanyRequest request,
            org.springframework.security.core.Authentication authentication) {

        try {

            User user =
                    getLoggedInUser(authentication);

            Company updated =
                    companyService.updateCompany(
                            user,
                            request
                    );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ErrorResponse(
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET LOGGED-IN USER
    // =========================================================

    private User getLoggedInUser(
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new RuntimeException(
                    "User is not authenticated."
            );
        }

        return userRepository
                .findByUsernameWithCompany(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found."
                        )
                );
    }

    // =========================================================
    // STAFF RESPONSE
    // =========================================================

    public static class StaffCompanyResponse {

        private String companyName;

        public StaffCompanyResponse(
                String companyName) {

            this.companyName = companyName;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(
                String companyName) {

            this.companyName = companyName;
        }
    }

    // =========================================================
    // ERROR RESPONSE
    // =========================================================

    public static class ErrorResponse {

        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}