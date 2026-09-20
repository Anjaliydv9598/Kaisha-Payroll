package com.kaisha.payroll.salary.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SalaryResponse {

    private Long salaryId;

    private String employeeId;

    private String payPeriod;

    private BigDecimal grossSalary;

    private BigDecimal totalDeductions;

    private BigDecimal netSalary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<SalaryComponentDto>
            components = new ArrayList<>();





}