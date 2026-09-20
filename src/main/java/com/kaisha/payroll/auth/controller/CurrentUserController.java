package com.kaisha.payroll.auth.controller;

import com.kaisha.payroll.auth.dto.CurrentUserResponse;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.CurrentUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        User user = currentUserService.getCurrentUser();

        String role = user.getRole().name();

        String companyId = null;
        String companyName = null;
        String location = null;

        if (user.getCompany() != null) {

            companyId = user.getCompany().getCompanyId();

            companyName = user.getCompany().getCompanyName();

            /*
             * Only ADMIN receives company address/location.
             *
             * STAFF will receive company name,
             * but location will remain null.
             */
            if ("ADMIN".equals(role)) {

                location = user.getCompany().getAddress();
            }
        }

        CurrentUserResponse response =
                new CurrentUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getMobileNumber(),
                        role,
                        companyId,
                        companyName,
                        location
                );

        return ResponseEntity.ok(response);
    }
}