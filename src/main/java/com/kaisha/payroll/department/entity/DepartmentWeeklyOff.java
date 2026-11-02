package com.kaisha.payroll.department.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;

@Entity
@Table(
        name = "department_weekly_offs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_weekly_off",
                        columnNames = {"department_id", "day_of_week"}
                )
        }
)
public class DepartmentWeeklyOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    public DepartmentWeeklyOff() {
    }

    public DepartmentWeeklyOff(Department department, DayOfWeek dayOfWeek) {
        this.department = department;
        this.dayOfWeek = dayOfWeek;
    }

    public Long getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}