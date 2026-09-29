package com.kaisha.payroll.attendance.dto;

import com.kaisha.payroll.attendance.entity.Attendance;
import com.kaisha.payroll.attendance.entity.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AttendanceResponse {

    private Long id;

    private String employeeId;
    private String employeeName;
    private String department;

    private LocalDate attendanceDate;

    private LocalTime punchIn;
    private LocalTime punchOut;

    private BigDecimal workingHours;

    private AttendanceStatus status;

    private String correctionReason;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

    public AttendanceResponse() {
    }

    public AttendanceResponse(Attendance attendance) {

        this.id = attendance.getId();

        if (attendance.getEmployee() != null) {
            this.employeeId = attendance.getEmployee().getEmployeeId();

            /*
             * Employee name/department are stored as EmployeeField
             * in your current project.
             *
             * These can be populated later from EmployeeField.
             */
            this.employeeName = null;
            this.department = null;
        }

        this.attendanceDate = attendance.getAttendanceDate();
        this.punchIn = attendance.getPunchIn();
        this.punchOut = attendance.getPunchOut();
        this.workingHours = attendance.getWorkingHours();
        this.status = attendance.getStatus();
        this.correctionReason = attendance.getCorrectionReason();
        this.modifiedBy = attendance.getModifiedBy();
        this.modifiedAt = attendance.getModifiedAt();
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public LocalTime getPunchIn() {
        return punchIn;
    }

    public LocalTime getPunchOut() {
        return punchOut;
    }

    public BigDecimal getWorkingHours() {
        return workingHours;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public String getCorrectionReason() {
        return correctionReason;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
}