package com.kaisha.payroll.payroll.dto;

import com.kaisha.payroll.payroll.request.PayrollRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PayrollDownloadRequestDto {

    @NotBlank
    private String payPeriod;

    @NotNull
    private PayrollRequestType scope;

    /*
     * E001,E002,E005
     */
    private String employeeIds;

    private Integer fromRecord;

    private Integer toRecord;

    @NotBlank
    private String format;

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public PayrollRequestType getScope() {
        return scope;
    }

    public void setScope(PayrollRequestType scope) {
        this.scope = scope;
    }

    public String getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(String employeeIds) {
        this.employeeIds = employeeIds;
    }

    public Integer getFromRecord() {
        return fromRecord;
    }

    public void setFromRecord(Integer fromRecord) {
        this.fromRecord = fromRecord;
    }

    public Integer getToRecord() {
        return toRecord;
    }

    public void setToRecord(Integer toRecord) {
        this.toRecord = toRecord;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}