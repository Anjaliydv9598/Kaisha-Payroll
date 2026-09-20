package com.kaisha.payroll.company.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company")
public class Company {

    @Id
    @Column(name = "company_id", length = 50)
    private String companyId;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "telephone_number", length = 30)
    private String telephoneNumber;

    @Column(name = "pan_no", length = 20)
    private String panNo;

    @Column(name = "tan_no", length = 20)
    private String tanNo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

}
