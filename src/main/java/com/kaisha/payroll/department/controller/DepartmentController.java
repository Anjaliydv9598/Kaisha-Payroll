package com.kaisha.payroll.department.controller;

import com.kaisha.payroll.department.dto.DepartmentRequest;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.service.DepartmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService) {

        this.departmentService = departmentService;
    }

    @GetMapping
    public ResponseEntity<?> getDepartments() {

        try {

            List<Department> departments =
                    departmentService.getDepartments();

            return ResponseEntity.ok(departments);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addDepartment(
            @RequestBody DepartmentRequest request) {

        try {

            return ResponseEntity.ok(
                    departmentService.addDepartment(request)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDepartment(
            @PathVariable Long departmentId,
            @RequestBody DepartmentRequest request) {

        try {

            return ResponseEntity.ok(
                    departmentService.updateDepartment(
                            departmentId,
                            request
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDepartment(
            @PathVariable Long departmentId) {

        try {

            departmentService.deleteDepartment(
                    departmentId
            );

            return ResponseEntity.ok(
                    "Department deleted successfully."
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}