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

    public AttendanceController(
            AttendanceService attendanceService
    ) {
        this.attendanceService = attendanceService;
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance(
            java.security.Principal principal
    ) {

        return ResponseEntity.ok(
                attendanceService.getAllAttendance(
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<AttendanceResponse> getAttendance(
            @PathVariable Long id,
            java.security.Principal principal
    ) {

        return ResponseEntity.ok(
                attendanceService.getAttendance(
                        id,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // GET EMPLOYEE ATTENDANCE
    // ---------------------------------------------------------

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<AttendanceResponse>>
    getEmployeeAttendance(
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
    // GET BY DATE
    // ---------------------------------------------------------

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<AttendanceResponse>>
    getByDate(
            @PathVariable LocalDate date,
            java.security.Principal principal
    ) {

        return ResponseEntity.ok(
                attendanceService.getByDate(
                        date,
                        principal.getName()
                )
        );
    }

    // ---------------------------------------------------------
    // GET DATE RANGE
    // ---------------------------------------------------------

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<AttendanceResponse>>
    getByDateRange(
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
    // CREATE
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
    // UPDATE
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
    // DELETE
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

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "Attendance deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // PUNCH IN
    // ---------------------------------------------------------

    @PostMapping("/punch-in/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
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
    // PUNCH OUT
    // ---------------------------------------------------------

    @PostMapping("/punch-out/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
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
    // MANUAL SYSTEM CHECK
    // ---------------------------------------------------------

    @PostMapping("/mark-missing-absent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>>
    markMissingPunchesAbsent() {

        int updated =
                attendanceService
                        .markMissingPunchesAbsent();

        Map<String, Object> response =
                new HashMap<>();

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
}