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

    public void setEmployeeId(
            String employeeId) {

        this.employeeId = employeeId;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(
            String payPeriod) {

        this.payPeriod = payPeriod;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(
            BigDecimal grossSalary) {

        this.grossSalary = grossSalary;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(
            BigDecimal totalDeductions) {

        this.totalDeductions =
                totalDeductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(
            BigDecimal netSalary) {

        this.netSalary = netSalary;
    }

    public Payroll.PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(
            Payroll.PayrollStatus status) {

        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(
            LocalDateTime processedAt) {

        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}