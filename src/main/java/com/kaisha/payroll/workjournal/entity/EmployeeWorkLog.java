
package com.kaisha.payroll.workjournal.entity;

import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.employee.entity.Employee;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_work_log",
        indexes = {
                @Index(
                        name = "idx_worklog_employee_date",
                        columnList = "employee_id, work_date"
                ),
                @Index(
                        name = "idx_worklog_company_date",
                        columnList = "company_id, work_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EmployeeWorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Employee who created the work entry
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Company to which the entry belongs
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Date of the work
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // Daily work description
    @Column(
            name = "work_description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String workDescription;

    // Important achievement
    @Column(name = "is_highlight", nullable = false)
    private boolean highlight = false;

    // Optional achievement category
    @Column(name = "highlight_category", length = 100)
    private String highlightCategory;

    // Record creation timestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Record update timestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
