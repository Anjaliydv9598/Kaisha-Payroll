package com.kaisha.payroll.employee.dto;

import com.kaisha.payroll.employee.entity.EmployeeChangeRequestStatus;
import com.kaisha.payroll.employee.entity.EmployeeChangeRequestType;

import java.time.LocalDateTime;

public class EmployeeChangeRequestResponse {

    private Long requestId;

    private String employeeId;

    private Long fieldId;

    private EmployeeChangeRequestType requestType;

    private String fieldName;

    private String oldFieldName;

    private String fieldValue;

    private String reason;

    private EmployeeChangeRequestStatus status;

    private String requestedBy;

    private LocalDateTime createdAt;

    public EmployeeChangeRequestResponse() {
    }

    public EmployeeChangeRequestResponse(
            Long requestId,
            String employeeId,
            Long fieldId,
            EmployeeChangeRequestType requestType,
            String fieldName,
            String oldFieldName,
            String fieldValue,
            String reason,
            EmployeeChangeRequestStatus status,
            String requestedBy,
            LocalDateTime createdAt
    ) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.fieldId = fieldId;
        this.requestType = requestType;
        this.fieldName = fieldName;
        this.oldFieldName = oldFieldName;
        this.fieldValue = fieldValue;
        this.reason = reason;
        this.status = status;
        this.requestedBy = requestedBy;
        this.createdAt = createdAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public EmployeeChangeRequestType getRequestType() {
        return requestType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldFieldName() {
        return oldFieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getReason() {
        return reason;
    }

    public EmployeeChangeRequestStatus getStatus() {
        return status;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public void setRequestType(EmployeeChangeRequestType requestType) {
        this.requestType = requestType;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setOldFieldName(String oldFieldName) {
        this.oldFieldName = oldFieldName;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(EmployeeChangeRequestStatus status) {
        this.status = status;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}