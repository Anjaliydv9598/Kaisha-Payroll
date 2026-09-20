package com.kaisha.payroll.auth.controller;

import com.kaisha.payroll.company.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class RegistrationStatusController {

    private final CompanyRepository companyRepository;

    public RegistrationStatusController(
            CompanyRepository companyRepository
    ) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/registration-status")
    public ResponseEntity<?> getRegistrationStatus() {

        boolean registrationAvailable =
                companyRepository.count() == 0;

        return ResponseEntity.ok(
                Map.of(
                        "registrationAvailable",
                        registrationAvailable
                )
        );
    }
}