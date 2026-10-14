package com.kaisha.payroll.employee.controller;

import com.kaisha.payroll.employee.dto.EmployeePrefixRequest;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(
            EmployeeService employeeService
    ) {
        this.employeeService = employeeService;
    }

    // ============================================================
    // GET ALL EMPLOYEES
    // ADMIN + STAFF
    // ============================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> getEmployees() {

        List<Employee> employees =
                employeeService.getEmployees();

        List<Map<String, Object>> response =
                employees.stream()
                        .map(employee -> {

                            Map<String, Object> map =
                                    new HashMap<>();

                            map.put(
                                    "employeeId",
                                    employee.getEmployeeId()
                            );

                            map.put(
                                    "active",
                                    employee.isActive()
                            );

                            return map;
                        })
                        .toList();

        return ResponseEntity.ok(
                response
        );
    }

    // ============================================================
    // CREATE EMPLOYEE
    // ADMIN ONLY
    //
    // Request:
    //
    // {
    //     "department": "HR"
    // }
    //
    // Response:
    //
    // {
    //     "message": "Employee created successfully",
    //     "employeeId": "HR001"
    // }
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(
            @RequestBody Map<String, String> body
    ) {

        String department =
                body.get("department");

        Employee employee =
                employeeService.createEmployee(
                        department
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Employee created successfully"
        );

        response.put(
                "employeeId",
                employee.getEmployeeId()
        );

        response.put(
                "department",
                department
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ============================================================
    // GET EMPLOYEE FIELDS
    // ADMIN + STAFF
    // ============================================================

    @GetMapping("/{employeeId}/fields")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<?> getEmployeeFields(
            @PathVariable String employeeId
    ) {

        return ResponseEntity.ok(
                employeeService.getEmployeeFields(
                        employeeId
                )
        );
    }

    // ============================================================
    // ADD EMPLOYEE FIELD
    // ADMIN ONLY
    // ============================================================

    @PostMapping("/{employeeId}/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addField(
            @PathVariable String employeeId,
            @RequestBody Map<String, String> body
    ) {

        EmployeeField field =
                employeeService.addField(
                        employeeId,
                        body.get("fieldName"),
                        body.get("fieldValue")
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(field);
    }

    // ============================================================
    // UPDATE EMPLOYEE FIELD
    // ADMIN ONLY
    // ============================================================

    @PutMapping("/{employeeId}/fields/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateField(
            @PathVariable String employeeId,
            @PathVariable Long fieldId,
            @RequestBody Map<String, String> body
    ) {

        EmployeeField field =
                employeeService.updateField(
                        employeeId,
                        fieldId,
                        body.get("fieldName"),
                        body.get("fieldValue")
                );

        return ResponseEntity.ok(
                field
        );
    }

    // ============================================================
    // DELETE EMPLOYEE FIELD
    // ADMIN ONLY
    // ============================================================

    @DeleteMapping("/{employeeId}/fields/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteField(
            @PathVariable String employeeId,
            @PathVariable Long fieldId
    ) {

        employeeService.deleteField(
                employeeId,
                fieldId
        );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "Employee field deleted successfully"
        );

        return ResponseEntity.ok(
                response
        );
    }

    // ============================================================
    // UPDATE EMPLOYEE PREFIX
    // ADMIN ONLY
    //
    // Example:
    //
    // HR001
    //     ↓
    // STAFF001
    // ============================================================

    @PutMapping("/{employeeId}/prefix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployeePrefix(
            @PathVariable String employeeId,
            @RequestBody EmployeePrefixRequest request
    ) {

        Employee employee =
                employeeService.updateEmployeePrefix(
                        employeeId,
                        request.getPrefix()
                );

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "Employee ID prefix updated successfully"
        );

        response.put(
                "employeeId",
                employee.getEmployeeId()
        );

        return ResponseEntity.ok(
                response
        );
    }
}