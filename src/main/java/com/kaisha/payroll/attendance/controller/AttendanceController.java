package com.kaisha.payroll.attendance.controller;

import com.kaisha.payroll.attendance.dto.AttendanceRequest;
import com.kaisha.payroll.attendance.dto.AttendanceResponse;
import com.kaisha.payroll.attendance.service.AttendanceService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ---------------------------------------------------------
    // ADMIN: GET ALL ATTENDANCE
    // ---------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance(
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getAllAttendance(principal.getName())
        );
    }

    // ---------------------------------------------------------
    // ADMIN: GET ATTENDANCE BY ID
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> getAttendance(
            @PathVariable Long id,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendance(id, principal.getName())
        );
    }

    // ---------------------------------------------------------
    // ADMIN: GET EMPLOYEE ATTENDANCE
    // ---------------------------------------------------------

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getEmployeeAttendance(
            @PathVariable String employeeId,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getEmployeeAttendance(
                        employeeId,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // ADMIN: GET ATTENDANCE BY DATE
    // ---------------------------------------------------------

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getByDate(
            @PathVariable LocalDate date,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getByDate(date, principal.getName())
        );
    }

    // ---------------------------------------------------------
    // ADMIN: GET ATTENDANCE BY DATE RANGE
    // ---------------------------------------------------------

    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getByDateRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getByDateRange(
                        from,
                        to,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // ADMIN: CREATE ATTENDANCE
    // ---------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> createAttendance(
            @RequestBody AttendanceRequest request,
            java.security.Principal principal
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        attendanceService.createAttendance(
                                request,
                                principal.getName()
                        )
                );
    }

    // ---------------------------------------------------------
    // ADMIN: UPDATE ATTENDANCE
    // ---------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceRequest request,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.updateAttendance(
                        id,
                        request,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // ADMIN: DELETE ATTENDANCE
    // ---------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAttendance(
            @PathVariable Long id,
            java.security.Principal principal
    ) {
        attendanceService.deleteAttendance(
                id,
                principal.getName()
        );

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                "Attendance deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // ADMIN: PUNCH IN FOR AN EMPLOYEE
    // ---------------------------------------------------------

    @PostMapping("/punch-in/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> punchIn(
            @PathVariable String employeeId,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.punchIn(
                        employeeId,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // ADMIN: PUNCH OUT FOR AN EMPLOYEE
    // ---------------------------------------------------------

    @PostMapping("/punch-out/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceResponse> punchOut(
            @PathVariable String employeeId,
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.punchOut(
                        employeeId,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // ADMIN: MARK MISSING PUNCHES ABSENT
    // ---------------------------------------------------------

    @PostMapping("/mark-missing-absent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> markMissingPunchesAbsent() {

        int updated = attendanceService.markMissingPunchesAbsent();

        Map<String, Object> response = new HashMap<>();

        response.put(
                "message",
                "Missing punches processed successfully"
        );

        response.put(
                "updatedRecords",
                updated
        );

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // STAFF: GET MY OWN ATTENDANCE
    // ---------------------------------------------------------

    @GetMapping("/staff/my-attendance")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendance(
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getMyAttendance(
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // STAFF: PUNCH IN
    // ---------------------------------------------------------

    @PostMapping("/staff/punch-in")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<AttendanceResponse> staffPunchIn(
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.staffPunchIn(
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // STAFF: PUNCH OUT
    // ---------------------------------------------------------

    @PostMapping("/staff/punch-out")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<AttendanceResponse> staffPunchOut(
            java.security.Principal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.staffPunchOut(
                        principal.getName()
                )
        );
    }
}