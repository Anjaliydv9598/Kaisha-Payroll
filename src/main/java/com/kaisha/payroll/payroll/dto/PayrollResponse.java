package com.kaisha.payroll.payroll.dto;

import com.kaisha.payroll.payroll.entity.Payroll;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollResponse {

    private Long payrollId;
    private String employeeId;
    private String payPeriod;

    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;

    private BigDecimal attendanceDeduction;
    private BigDecimal otherDeductions;

    private Integer presentDays;
    private Integer absentDays;
    private Integer halfDays;
    private Integer paidHolidays;
    private Integer weeklyOffs;
    private Integer paidLeaveDays;
    private Integer unpaidLeaveDays;

    private Payroll.PayrollStatus status;

    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    public Long getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(Long payrollId) {
        this.payrollId = payrollId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public BigDecimal getAttendanceDeduction() {
        return attendanceDeduction;
    }

    public void setAttendanceDeduction(BigDecimal attendanceDeduction) {
        this.attendanceDeduction = attendanceDeduction;
    }

    public BigDecimal getOtherDeductions() {
        return otherDeductions;
    }

    public void setOtherDeductions(BigDecimal otherDeductions) {
        this.otherDeductions = otherDeductions;
    }

    public Integer getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Integer presentDays) {
        this.presentDays = presentDays;
    }

    public Integer getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(Integer absentDays) {
        this.absentDays = absentDays;
    }

    public Integer getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(Integer halfDays) {
        this.halfDays = halfDays;
    }

    public Integer getPaidHolidays() {
        return paidHolidays;
    }

    public void setPaidHolidays(Integer paidHolidays) {
        this.paidHolidays = paidHolidays;
    }

    public Integer getWeeklyOffs() {
        return weeklyOffs;
    }

    public void setWeeklyOffs(Integer weeklyOffs) {
        this.weeklyOffs = weeklyOffs;
    }

    public Integer getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(Integer paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

    public Integer getUnpaidLeaveDays() {
        return unpaidLeaveDays;
    }

    public void setUnpaidLeaveDays(Integer unpaidLeaveDays) {
        this.unpaidLeaveDays = unpaidLeaveDays;
    }

    public Payroll.PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(Payroll.PayrollStatus status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}