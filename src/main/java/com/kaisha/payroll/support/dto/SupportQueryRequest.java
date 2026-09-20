package com.kaisha.payroll.support.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportQueryRequest {

    private String companyId;

    private String email;

    private String query;
}