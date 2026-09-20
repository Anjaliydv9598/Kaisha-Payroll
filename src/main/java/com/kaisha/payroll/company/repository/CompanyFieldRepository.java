package com.kaisha.payroll.company.repository;

import com.kaisha.payroll.company.entity.CompanyField;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyFieldRepository
        extends JpaRepository<CompanyField, Long> {

    // =========================================================
    // GET ACTIVE FIELDS FOR ONE COMPANY
    // =========================================================

    List<CompanyField>
    findByCompany_CompanyIdAndActiveTrue(
            String companyId
    );


    // =========================================================
    // CHECK DUPLICATE ACTIVE FIELD NAME
    // =========================================================

    boolean
    existsByCompany_CompanyIdAndFieldNameIgnoreCaseAndActiveTrue(
            String companyId,
            String fieldName
    );
}