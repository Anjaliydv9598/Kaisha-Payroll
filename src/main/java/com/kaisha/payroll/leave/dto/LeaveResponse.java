
package com.kaisha.payroll.leave.dto;

import com.kaisha.payroll.leave.entity.LeaveStatus;
import com.kaisha.payroll.leave.entity.LeaveType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        String employeeId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveType leaveType,
        LeaveStatus status,
        String reason,
        String adminRemark,
        LocalDateTime createdAt,
        LocalDateTime decidedAt
) {}
