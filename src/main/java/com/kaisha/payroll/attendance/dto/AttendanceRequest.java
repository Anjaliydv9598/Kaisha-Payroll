package com.kaisha.payroll.attendance.dto;

import com.kaisha.payroll.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRequest {

    private String employeeId;
    private LocalDate attendanceDate;
    private LocalTime punchIn;
    private LocalTime punchOut;
    private AttendanceStatus status;
    private String correctionReason;

    public AttendanceRequest() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalTime getPunchIn() {
        return punchIn;
    }

    public void setPunchIn(LocalTime punchIn) {
        this.punchIn = punchIn;
    }

    public LocalTime getPunchOut() {
        return punchOut;
    }

    public void setPunchOut(LocalTime punchOut) {
        this.punchOut = punchOut;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getCorrectionReason() {
        return correctionReason;
    }

    public void setCorrectionReason(String correctionReason) {
        this.correctionReason = correctionReason;
    }
}