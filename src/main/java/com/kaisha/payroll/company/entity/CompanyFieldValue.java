package com.kaisha.payroll.company.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_field_values")
public class CompanyFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "field_id",
            nullable = false
    )
    private CompanyField field;

    @Column(
            name = "field_value",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String fieldValue;

    public CompanyFieldValue() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CompanyField getField() {
        return field;
    }

    public void setField(CompanyField field) {
        this.field = field;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}