package com.kaisha.payroll.employee.numberseries.dto;

import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;

public class NumberSeriesResponse {

    private Long id;

    private String department;

    private String prefix;

    private Long startNumber;

    private Long endNumber;

    private Integer numberLength;

    private Long nextNumber;

    private String nextEmployeeId;

    private boolean active;

    public NumberSeriesResponse() {
    }

    public NumberSeriesResponse(
            EmployeeNumberSeries series
    ) {

        this.id =
                series.getId();

        this.department =
                series.getDepartment();

        this.prefix =
                series.getPrefix();

        this.startNumber =
                series.getStartNumber();

        this.endNumber =
                series.getEndNumber();

        this.numberLength =
                series.getNumberLength();

        this.nextNumber =
                series.getNextNumber();

        this.nextEmployeeId =
                buildEmployeeId(
                        series.getPrefix(),
                        series.getNextNumber(),
                        series.getNumberLength()
                );

        this.active =
                series.isActive();
    }

    private String buildEmployeeId(
            String prefix,
            Long number,
            Integer length
    ) {

        if (
                prefix == null
                        ||
                        number == null
                        ||
                        length == null
        ) {

            return null;
        }

        return prefix +
                String.format(
                        "%0" + length + "d",
                        number
                );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNextEmployeeId() {
        return nextEmployeeId;
    }

    public void setNextEmployeeId(String nextEmployeeId) {
        this.nextEmployeeId = nextEmployeeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}