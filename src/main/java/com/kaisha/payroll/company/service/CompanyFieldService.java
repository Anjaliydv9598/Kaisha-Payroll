package com.kaisha.payroll.company.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.dto.CompanyFieldRequest;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.entity.CompanyField;
import com.kaisha.payroll.company.entity.CompanyFieldValue;
import com.kaisha.payroll.company.repository.CompanyFieldRepository;
import com.kaisha.payroll.company.repository.CompanyFieldValueRepository;
import com.kaisha.payroll.company.repository.CompanyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyFieldService {

    private final CompanyFieldRepository companyFieldRepository;
    private final CompanyFieldValueRepository companyFieldValueRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;

    public CompanyFieldService(
            CompanyFieldRepository companyFieldRepository,
            CompanyFieldValueRepository companyFieldValueRepository,
            CompanyRepository companyRepository,
            CurrentUserService currentUserService
    ) {
        this.companyFieldRepository =
                companyFieldRepository;

        this.companyFieldValueRepository =
                companyFieldValueRepository;

        this.companyRepository =
                companyRepository;

        this.currentUserService =
                currentUserService;
    }


    // =========================================================
    // GET ALL ACTIVE DYNAMIC FIELDS
    // =========================================================

    @Transactional(readOnly = true)
    public List<CompanyField> getFieldsForCurrentCompany() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return companyFieldRepository
                .findByCompany_CompanyIdAndActiveTrue(
                        companyId
                );
    }


    // =========================================================
    // ADD NEW FIELD
    // =========================================================

    @Transactional
    public CompanyField addField(
            CompanyFieldRequest request
    ) {

        validateRequest(request);

        String companyId =
                currentUserService.getCurrentCompanyId();

        Company company =
                companyRepository
                        .findByCompanyId(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found: "
                                                + companyId
                                )
                        );


        String fieldName =
                request.getFieldName().trim();

        String fieldValue =
                request.getFieldValue().trim();


        CompanyField field =
                new CompanyField();

        field.setCompany(company);
        field.setFieldName(fieldName);
        field.setActive(true);


        CompanyField savedField =
                companyFieldRepository.save(field);


        CompanyFieldValue value =
                new CompanyFieldValue();

        value.setField(savedField);
        value.setFieldValue(fieldValue);

        companyFieldValueRepository.save(value);


        return savedField;
    }


    // =========================================================
    // UPDATE FIELD
    // =========================================================

    @Transactional
    public CompanyField updateField(
            Long fieldId,
            CompanyFieldRequest request
    ) {

        validateRequest(request);

        String companyId =
                currentUserService.getCurrentCompanyId();


        CompanyField field =
                companyFieldRepository
                        .findById(fieldId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company field not found: "
                                                + fieldId
                                )
                        );


        /*
         * SECURITY:
         *
         * Make sure this field belongs to
         * the logged-in user's company.
         */

        if (field.getCompany() == null ||
                !companyId.equals(
                        field.getCompany().getCompanyId()
                )) {

            throw new RuntimeException(
                    "You are not authorized to update this field."
            );
        }


        if (!field.isActive()) {

            throw new RuntimeException(
                    "This company field is inactive."
            );
        }


        field.setFieldName(
                request.getFieldName().trim()
        );


        CompanyField savedField =
                companyFieldRepository.save(field);


        CompanyFieldValue value =
                companyFieldValueRepository
                        .findFirstByField_FieldId(fieldId)
                        .orElseGet(() -> {

                            CompanyFieldValue newValue =
                                    new CompanyFieldValue();

                            newValue.setField(savedField);

                            return newValue;
                        });


        value.setFieldValue(
                request.getFieldValue().trim()
        );

        companyFieldValueRepository.save(value);


        return savedField;
    }


    // =========================================================
    // DELETE ONE DYNAMIC FIELD
    // =========================================================

    @Transactional
    public void deleteField(Long fieldId) {

        String companyId =
                currentUserService.getCurrentCompanyId();


        CompanyField field =
                companyFieldRepository
                        .findById(fieldId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company field not found: "
                                                + fieldId
                                )
                        );


        /*
         * SECURITY:
         *
         * A user cannot delete a field
         * belonging to another company.
         */

        if (field.getCompany() == null ||
                !companyId.equals(
                        field.getCompany().getCompanyId()
                )) {

            throw new RuntimeException(
                    "You are not authorized to delete this field."
            );
        }


        /*
         * Soft delete.
         *
         * The company itself is NOT deleted.
         */

        field.setActive(false);

        companyFieldRepository.save(field);
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateRequest(
            CompanyFieldRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Company field request is required."
            );
        }


        if (request.getFieldName() == null ||
                request.getFieldName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Field Name is required."
            );
        }


        if (request.getFieldValue() == null ||
                request.getFieldValue().trim().isEmpty()) {

            throw new RuntimeException(
                    "Value of Field is required."
            );
        }


        if (request.getFieldName().trim().length() > 100) {

            throw new RuntimeException(
                    "Field Name cannot exceed 100 characters."
            );
        }
    }
}