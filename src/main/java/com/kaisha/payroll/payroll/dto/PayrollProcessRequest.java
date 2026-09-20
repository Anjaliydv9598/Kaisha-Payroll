package com.kaisha.payroll.payroll.dto;

public class PayrollProcessRequest {

    private String payPeriod;

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(
            String payPeriod) {

        this.payPeriod =
                payPeriod;
    }
}