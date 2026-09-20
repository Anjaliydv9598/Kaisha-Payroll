package com.kaisha.payroll.company.repository;

import com.kaisha.payroll.company.entity.CompanyFieldValue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyFieldValueRepository
        extends JpaRepository<CompanyFieldValue, Long> {

    Optional<CompanyFieldValue>
    findFirstByField_FieldId(Long fieldId);
}