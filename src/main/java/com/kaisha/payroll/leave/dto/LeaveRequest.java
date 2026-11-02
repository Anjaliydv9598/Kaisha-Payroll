
package com.kaisha.payroll.leave.dto;

import com.kaisha.payroll.leave.entity.LeaveType;
import java.time.LocalDate;

public record LeaveRequest(
        String employeeId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveType leaveType,
        String reason
) {}
