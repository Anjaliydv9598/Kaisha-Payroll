package com.kaisha.payroll.auth.entity;

import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.employee.entity.Employee;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_employee",
                        columnNames = {"employee_id"}
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            unique = true,
            length = 150
    )
    private String email;

    @Column(
            name = "mobile_number",
            unique = true,
            length = 20
    )
    private String mobileNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Role role;

    // Company relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private Company company;

    // Optional Employee relationship.
    // ADMIN users can have no Employee.
    // Each Employee can have at most one User login.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id"
    )
    private Employee employee;

    @Column(nullable = false)
    private boolean active = true;
}