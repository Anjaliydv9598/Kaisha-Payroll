
package com.kaisha.payroll.leave.dto;

import com.kaisha.payroll.leave.entity.LeaveStatus;

public record LeaveDecisionRequest(
        LeaveStatus status,
        String adminRemark
) {}
