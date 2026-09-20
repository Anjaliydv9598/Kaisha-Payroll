package com.kaisha.payroll.company.dto;

public class CompanyFieldResponse {

    private Long fieldId;
    private String fieldName;
    private String fieldValue;

    public CompanyFieldResponse() {
    }

    public CompanyFieldResponse(
            Long fieldId,
            String fieldName,
            String fieldValue) {

        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
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