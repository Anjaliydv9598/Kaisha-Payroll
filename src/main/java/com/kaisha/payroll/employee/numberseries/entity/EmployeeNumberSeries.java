package com.kaisha.payroll.employee.numberseries.entity;

import com.kaisha.payroll.company.entity.Company;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_number_series",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_number_series_company_department",
                        columnNames = {"company_id", "department"}
                )
        }
)
public class EmployeeNumberSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Company to which this number series belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "company_id",
            nullable = false
    )
    private Company company;

    /*
     * Department name.
     *
     * Examples:
     * HR
     * Finance
     * CO
     * PP
     */
    @Column(
            name = "department",
            nullable = false,
            length = 100
    )
    private String department;

    /*
     * Employee ID prefix.
     *
     * Examples:
     * HR
     * FIN
     * CO
     * PP
     */
    @Column(
            name = "prefix",
            nullable = false,
            length = 20
    )
    private String prefix;

    /*
     * Starting numeric value.
     *
     * Example:
     * 1
     *
     * Displayed as 001 when numberLength = 3.
     */
    @Column(
            name = "start_number",
            nullable = false
    )
    private Long startNumber;

    /*
     * Maximum numeric value.
     *
     * Example:
     * 1000
     */
    @Column(
            name = "end_number",
            nullable = false
    )
    private Long endNumber;

    /*
     * Number of digits.
     *
     * Example:
     * 3
     *
     * 1     -> 1
     * 3     -> 001
     * 4     -> 0001
     */
    @Column(
            name = "number_length",
            nullable = false
    )
    private Integer numberLength;

    /*
     * Next number which will be generated.
     *
     * Example:
     *
     * startNumber = 1
     * nextNumber = 1
     *
     * After HR001 is generated:
     * nextNumber = 2
     */
    @Column(
            name = "next_number",
            nullable = false
    )
    private Long nextNumber;

    /*
     * Whether this series is currently usable.
     */
    @Column(
            name = "active",
            nullable = false
    )
    private boolean active = true;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public EmployeeNumberSeries() {
    }

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(Long startNumber) {
        this.startNumber = startNumber;
    }

    public Long getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(Long endNumber) {
        this.endNumber = endNumber;
    }

    public Integer getNumberLength() {
        return numberLength;
    }

    public void setNumberLength(Integer numberLength) {
        this.numberLength = numberLength;
    }

    public Long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(Long nextNumber) {
        this.nextNumber = nextNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}