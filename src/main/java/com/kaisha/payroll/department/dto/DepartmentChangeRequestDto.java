package com.kaisha.payroll.department.dto;

import com.kaisha.payroll.department.entity.DepartmentChangeRequestType;

public class DepartmentChangeRequestDto {

    private DepartmentChangeRequestType requestType;

    private Long departmentId;

    private Long positionId;

    private String requestedName;

    private String reason;

    public DepartmentChangeRequestDto() {
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

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getRequestedName() {
        return requestedName;
    }

    public void setRequestedName(String requestedName) {
        this.requestedName = requestedName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}