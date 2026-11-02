package com.kaisha.payroll.department.controller;

import com.kaisha.payroll.department.dto.WeeklyOffRequest;
import com.kaisha.payroll.department.service.DepartmentWeeklyOffService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "http://localhost:5173")
public class DepartmentWeeklyOffController {

    private final DepartmentWeeklyOffService weeklyOffService;

    public DepartmentWeeklyOffController(
            DepartmentWeeklyOffService weeklyOffService) {
        this.weeklyOffService = weeklyOffService;
    }

    @GetMapping("/{departmentId}/weekly-offs")
    public ResponseEntity<?> getWeeklyOffs(
            @PathVariable Long departmentId) {

        try {
            List<DayOfWeek> days =
                    weeklyOffService.getWeeklyOffs(departmentId);

            return ResponseEntity.ok(days);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    @PutMapping("/{departmentId}/weekly-offs")
    public ResponseEntity<?> updateWeeklyOffs(
            @PathVariable Long departmentId,
            @RequestBody WeeklyOffRequest request) {

        try {
            List<DayOfWeek> days =
                    weeklyOffService.updateWeeklyOffs(
                            departmentId,
                            request
                    );

            return ResponseEntity.ok(days);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }
}