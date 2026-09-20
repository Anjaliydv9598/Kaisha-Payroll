package com.kaisha.payroll.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {

    private Long id;
    private String username;
    private String email;
    private String mobileNumber;
    private String role;
    private String companyId;
    private String companyName;
    private String location;
}