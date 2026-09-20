package com.kaisha.payroll.salary.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "salary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_salary_employee_period",
                        columnNames = {
                                "employee_id",
                                "pay_period"
                        }
                )
        }
)
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salaryId;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "pay_period", nullable = false)
    private String payPeriod;

    @Column(
            name = "gross_salary",
            precision = 15,
            scale = 2
    )
    private BigDecimal grossSalary = BigDecimal.ZERO;

    @Column(
            name = "total_deductions",
            precision = 15,
            scale = 2
    )
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(
            name = "net_salary",
            precision = 15,
            scale = 2
    )
    private BigDecimal netSalary = BigDecimal.ZERO;

    @OneToMany(
            mappedBy = "salary",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SalaryComponent> components =
            new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    public Salary() {
    }

    // =====================================================
    // COMPONENT METHODS
    // =====================================================

    public void addComponent(
            SalaryComponent component) {

        if (component == null) {
            return;
        }

        components.add(component);
        component.setSalary(this);
    }

    public void clearComponents() {

        for (SalaryComponent component : components) {
            component.setSalary(null);
        }

        components.clear();
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Long salaryId) {
        this.salaryId = salaryId;
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

    public void setTotalDeductions(
            BigDecimal totalDeductions) {

        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public List<SalaryComponent> getComponents() {
        return components;
    }

    public void setComponents(
            List<SalaryComponent> components) {

        this.components = components;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}