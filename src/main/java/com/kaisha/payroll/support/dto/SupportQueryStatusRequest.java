package com.kaisha.payroll.support.dto;

import com.kaisha.payroll.support.entity.SupportQueryStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportQueryStatusRequest {

    private SupportQueryStatus status;
}