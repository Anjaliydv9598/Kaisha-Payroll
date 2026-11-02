package com.kaisha.payroll.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payroll",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payroll_company_employee_period",
                        columnNames = {
                                "company_id",
                                "employee_id",
                                "pay_period"
                        }
                )
        }
)
@Getter
@Setter
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(name = "company_id", nullable = false, length = 100)
    private String companyId;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal attendanceDeduction = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    private Integer presentDays;
    private Integer absentDays;
    private Integer halfDays;
    private Integer paidHolidays;
    private Integer weeklyOffs;
    private Integer paidLeaveDays;
    private Integer unpaidLeaveDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.READY;

    private LocalDateTime processedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum PayrollStatus {
        READY,
        PROCESSED,
        CANCELLED
    }


}