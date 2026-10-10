package com.kaisha.payroll.employee.entity;

import com.kaisha.payroll.company.entity.Company;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_company_id",
                columnNames = {"company_id", "employee_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "details_completed", nullable = false)
    private boolean detailsCompleted = true;
}