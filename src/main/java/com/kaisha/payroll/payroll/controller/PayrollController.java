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

    public PayrollController(
            PayrollService payrollService) {

        this.payrollService =
                payrollService;
    }

    @PostMapping("/process")
    public ResponseEntity<List<PayrollResponse>>
    process(
            @RequestBody PayrollProcessRequest request) {

        return ResponseEntity.ok(
                payrollService.processPayroll(
                        request.getPayPeriod()
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<PayrollResponse>>
    getAll() {

        return ResponseEntity.ok(
                payrollService.getAll()
        );
    }

    @GetMapping("/{payrollId}")
    public ResponseEntity<PayrollResponse>
    getById(
            @PathVariable Long payrollId) {

        return ResponseEntity.ok(
                payrollService.getById(
                        payrollId
                )
        );
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollResponse>>
    getByEmployee(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                payrollService.getByEmployee(
                        employeeId
                )
        );
    }

    @GetMapping("/period/{payPeriod}")
    public ResponseEntity<List<PayrollResponse>>
    getByPeriod(
            @PathVariable String payPeriod) {

        return ResponseEntity.ok(
                payrollService.getByPeriod(
                        payPeriod
                )
        );
    }

    @PutMapping("/{payrollId}/cancel")
    public ResponseEntity<PayrollResponse>
    cancel(
            @PathVariable Long payrollId) {

        return ResponseEntity.ok(
                payrollService.cancel(
                        payrollId
                )
        );
    }
}