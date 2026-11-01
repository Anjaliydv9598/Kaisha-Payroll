package com.kaisha.payroll.masterdata.dto;

import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;

import java.util.List;

public class MasterDataResponse {

    private List<Department> departments;
    private List<EmployeeNumberSeries> numberSeries;

    public MasterDataResponse() {}

    public MasterDataResponse(List<Department> departments, List<EmployeeNumberSeries> numberSeries) {
        this.departments = departments;
        this.numberSeries = numberSeries;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<EmployeeNumberSeries> getNumberSeries() {
        return numberSeries;
    }

    public void setNumberSeries(List<EmployeeNumberSeries> numberSeries) {
        this.numberSeries = numberSeries;
    }
}