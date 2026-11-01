package com.kaisha.payroll.masterdata.dto;

import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesResponse;

import java.util.List;

public class MasterDataResponse {

    private List<Department> departments;
    private List<NumberSeriesResponse> numberSeries;

    public MasterDataResponse() {}

    public MasterDataResponse(List<Department> departments, List<NumberSeriesResponse> numberSeries) {
        this.departments = departments;
        this.numberSeries = numberSeries;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<NumberSeriesResponse> getNumberSeries() {
        return numberSeries;
    }

    public void setNumberSeries(List<NumberSeriesResponse> numberSeries) {
        this.numberSeries = numberSeries;
    }
}