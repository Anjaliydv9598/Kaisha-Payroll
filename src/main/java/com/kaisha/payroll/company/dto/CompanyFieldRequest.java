package com.kaisha.payroll.company.dto;

public class CompanyFieldRequest {

    private String fieldName;
    private String fieldValue;

    public CompanyFieldRequest() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}