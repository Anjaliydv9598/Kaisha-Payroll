package com.kaisha.payroll.masterdata.controller;

import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.service.DepartmentService;
import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;
import com.kaisha.payroll.employee.numberseries.service.EmployeeNumberSeriesService;
import com.kaisha.payroll.masterdata.dto.MasterDataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master-data")
@CrossOrigin
public class MasterDataController {

    private final DepartmentService departmentService;
    private final EmployeeNumberSeriesService numberSeriesService;

    public MasterDataController(
            DepartmentService departmentService,
            EmployeeNumberSeriesService numberSeriesService
    ) {
        this.departmentService = departmentService;
        this.numberSeriesService = numberSeriesService;
    }

    // Handles: GET /api/master-data/departments
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getDepartments() {
        try {
            List<Department> departments = departmentService.getDepartments();
            return ResponseEntity.ok(departments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Handles: GET /api/master-data (returns both departments & series)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> getMasterData() {
        try {
            List<Department> departments = departmentService.getDepartments();
            List<EmployeeNumberSeries> series = numberSeriesService.getAllSeries();

            return ResponseEntity.ok(new MasterDataResponse(departments, series));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}