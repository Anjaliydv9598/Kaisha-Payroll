package com.kaisha.payroll.salary.controller;

import com.kaisha.payroll.salary.dto.SalaryRequest;
import com.kaisha.payroll.salary.dto.SalaryResponse;
import com.kaisha.payroll.salary.service.SalaryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "http://localhost:5173")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(
            SalaryService salaryService) {

        this.salaryService =
                salaryService;
    }

    @GetMapping
    public ResponseEntity<List<SalaryResponse>>
    getAll() {

        return ResponseEntity.ok(
                salaryService.getAll()
        );
    }

    @GetMapping("/{salaryId}")
    public ResponseEntity<SalaryResponse>
    getById(
            @PathVariable Long salaryId) {

        return ResponseEntity.ok(
                salaryService.getById(
                        salaryId
                )
        );
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<SalaryResponse>>
    getByEmployee(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                salaryService.getByEmployee(
                        employeeId
                )
        );
    }

    @GetMapping("/period/{payPeriod}")
    public ResponseEntity<List<SalaryResponse>>
    getByPeriod(
            @PathVariable String payPeriod) {

        return ResponseEntity.ok(
                salaryService.getByPeriod(
                        payPeriod
                )
        );
    }

    @PostMapping
    public ResponseEntity<SalaryResponse>
    create(
            @RequestBody SalaryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        salaryService.create(
                                request
                        )
                );
    }

    @PutMapping("/{salaryId}")
    public ResponseEntity<SalaryResponse>
    update(
            @PathVariable Long salaryId,
            @RequestBody SalaryRequest request) {

        return ResponseEntity.ok(
                salaryService.update(
                        salaryId,
                        request
                )
        );
    }

    @DeleteMapping("/{salaryId}")
    public ResponseEntity<Void>
    delete(
            @PathVariable Long salaryId) {

        salaryService.delete(
                salaryId
        );

        return ResponseEntity.noContent()
                .build();
    }
}