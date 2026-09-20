package com.kaisha.payroll.department.dto;

public class PositionResponse {

    private Long positionId;
    private Long departmentId;
    private String departmentName;
    private String positionName;
    private boolean active;

    public PositionResponse() {
    }

    public PositionResponse(
            Long positionId,
            Long departmentId,
            String departmentName,
            String positionName,
            boolean active) {

        this.positionId = positionId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.active = active;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
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

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}