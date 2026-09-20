package com.kaisha.payroll.company.repository;

import com.kaisha.payroll.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository
        extends JpaRepository<Company, String> {

    boolean existsByCompanyId(String companyId);

    Optional<Company> findByCompanyId(String companyId);
}