
package com.kaisha.payroll.attendance.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AttendanceMonthlySummary {

    private String employeeId;
    private int year;
    private int month;

    private int scheduledWorkingDays;
    private int presentDays;
    private int absentDays;
    private int halfDays;
    private int paidHolidays;
    private int weeklyOffs;
    private int paidLeaveDays;
    private int unpaidLeaveDays;
    private int pendingMissingPunches;

    private BigDecimal unpaidDays = BigDecimal.ZERO;
    private BigDecimal dailyRate = BigDecimal.ZERO;
    private BigDecimal attendanceDeduction = BigDecimal.ZERO;


}
