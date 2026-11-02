package com.kaisha.payroll.salary.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "salary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_salary_company_employee_period",
                        columnNames = {
                                "company_id",
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

    @Column(name = "company_id", nullable = false, length = 100)
    private String companyId;
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


}