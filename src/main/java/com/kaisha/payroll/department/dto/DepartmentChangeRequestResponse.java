package com.kaisha.payroll.department.dto;

import com.kaisha.payroll.department.entity.DepartmentChangeRequestStatus;
import com.kaisha.payroll.department.entity.DepartmentChangeRequestType;

import java.time.LocalDateTime;

public class DepartmentChangeRequestResponse {

    private Long requestId;

    private DepartmentChangeRequestType requestType;

    private Long departmentId;

    private String departmentName;

    private Long positionId;

    private String positionName;

    private String requestedName;

    private String oldName;

    private String reason;

    private DepartmentChangeRequestStatus status;

    private String requestedBy;

    private LocalDateTime createdAt;

    public DepartmentChangeRequestResponse() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public DepartmentChangeRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(
            DepartmentChangeRequestType requestType) {

        this.requestType = requestType;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getRequestedName() {
        return requestedName;
    }

    public void setRequestedName(String requestedName) {
        this.requestedName = requestedName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DepartmentChangeRequestStatus getStatus() {
        return status;
    }

    public void setStatus(
            DepartmentChangeRequestStatus status) {

        this.status = status;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}