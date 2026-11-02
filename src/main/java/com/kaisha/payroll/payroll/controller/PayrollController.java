
package com.kaisha.payroll.payroll.controller;

import com.kaisha.payroll.payroll.dto.PayrollProcessRequest;
import com.kaisha.payroll.payroll.dto.PayrollResponse;
import com.kaisha.payroll.payroll.service.PayrollService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin(origins = "http://localhost:5173")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // PROCESS PAYROLL
    @PostMapping("/process")
    public ResponseEntity<List<PayrollResponse>> process(
            @RequestBody(required = false) PayrollProcessRequest request) {

        if (request == null ||
                request.getPayPeriod() == null ||
                request.getPayPeriod().isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                payrollService.processPayroll(
                        request.getPayPeriod().trim()
                )
        );
    }

    // GET ALL PAYROLL
    @GetMapping
    public ResponseEntity<List<PayrollResponse>> getAll() {

        return ResponseEntity.ok(
                payrollService.getAll()
        );
    }

    // GET PAYROLL BY ID
    @GetMapping("/{payrollId}")
    public ResponseEntity<PayrollResponse> getById(
            @PathVariable Long payrollId) {

        return ResponseEntity.ok(
                payrollService.getById(payrollId)
        );
    }

    // GET PAYROLL BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollResponse>> getByEmployee(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                payrollService.getByEmployee(employeeId)
        );
    }

    // GET PAYROLL BY PERIOD
    @GetMapping("/period/{payPeriod}")
    public ResponseEntity<List<PayrollResponse>> getByPeriod(
            @PathVariable String payPeriod) {

        return ResponseEntity.ok(
                payrollService.getByPeriod(payPeriod)
        );
    }

    // CANCEL PAYROLL
    @PutMapping("/{payrollId}/cancel")
    public ResponseEntity<PayrollResponse> cancel(
            @PathVariable Long payrollId) {

        return ResponseEntity.ok(
                payrollService.cancel(payrollId)
        );
    }
}
