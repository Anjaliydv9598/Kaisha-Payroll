package com.kaisha.payroll.payroll.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payroll",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payroll_employee_period",
                        columnNames = {
                                "employee_id",
                                "pay_period"
                        }
                )
        }
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    @Column(
            name = "employee_id",
            nullable = false,
            length = 50
    )
    private String employeeId;

    @Column(
            name = "pay_period",
            nullable = false,
            length = 7
    )
    private String payPeriod;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal grossSalary;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalDeductions;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private PayrollStatus status;

    private LocalDateTime processedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        createdAt =
                LocalDateTime.now();
    }

    public enum PayrollStatus {
        READY,
        PROCESSED,
        CANCELLED
    }

    public Long getPayrollId() {
        return payrollId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            String employeeId) {

        this.employeeId =
                employeeId;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(
            String payPeriod) {

        this.payPeriod =
                payPeriod;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(
            BigDecimal grossSalary) {

        this.grossSalary =
                grossSalary;
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

        this.netSalary =
                netSalary;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(
            PayrollStatus status) {

        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(
            LocalDateTime processedAt) {

        this.processedAt =
                processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}