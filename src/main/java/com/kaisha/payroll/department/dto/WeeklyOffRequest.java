package com.kaisha.payroll.department.dto;

import java.time.DayOfWeek;
import java.util.List;

public class WeeklyOffRequest {

    private List<DayOfWeek> days;

    public WeeklyOffRequest() {
    }

    public List<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(List<DayOfWeek> days) {
        this.days = days;
    }
}