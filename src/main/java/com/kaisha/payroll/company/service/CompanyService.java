package com.kaisha.payroll.company.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.company.dto.CompanyFieldRequest;
import com.kaisha.payroll.company.dto.CompanyFieldResponse;
import com.kaisha.payroll.company.dto.CompanyRequest;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.entity.CompanyField;
import com.kaisha.payroll.company.entity.CompanyFieldValue;
import com.kaisha.payroll.company.repository.CompanyFieldRepository;
import com.kaisha.payroll.company.repository.CompanyFieldValueRepository;
import com.kaisha.payroll.company.repository.CompanyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyFieldRepository companyFieldRepository;
    private final CompanyFieldValueRepository companyFieldValueRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyFieldRepository companyFieldRepository,
            CompanyFieldValueRepository companyFieldValueRepository) {

        this.companyRepository = companyRepository;
        this.companyFieldRepository = companyFieldRepository;
        this.companyFieldValueRepository = companyFieldValueRepository;
    }

    // =========================================================
    // GET COMPANY FOR LOGGED-IN USER
    // =========================================================

    public Company getCompanyForUser(User user) {

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "No company is assigned to this user."
            );
        }

        return user.getCompany();
    }

    // =========================================================
    // UPDATE COMPANY
    // ADMIN ONLY
    // =========================================================

    @Transactional
    public Company updateCompany(
            User user,
            CompanyRequest request) {

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getRole() == null ||
                !"ADMIN".equals(user.getRole().name())) {

            throw new RuntimeException(
                    "Only ADMIN can update company information."
            );
        }

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "No company is assigned to this user."
            );
        }

        if (request == null) {
            throw new RuntimeException(
                    "Company information is required."
            );
        }

        if (request.getCompanyName() == null ||
                request.getCompanyName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company name is required."
            );
        }

        if (request.getAddress() == null ||
                request.getAddress().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company address/location is required."
            );
        }

        Company company = user.getCompany();

        company.setCompanyName(
                request.getCompanyName().trim()
        );

        company.setAddress(
                request.getAddress().trim()
        );

        if (request.getTelephoneNumber() != null) {

            company.setTelephoneNumber(
                    request.getTelephoneNumber().trim()
            );
        }

        if (request.getPanNo() != null) {

            String pan =
                    request.getPanNo().trim();

            company.setPanNo(
                    pan.isEmpty()
                            ? null
                            : pan.toUpperCase()
            );
        }

        if (request.getTanNo() != null) {

            String tan =
                    request.getTanNo().trim();

            company.setTanNo(
                    tan.isEmpty()
                            ? null
                            : tan.toUpperCase()
            );
        }

        return companyRepository.save(company);
    }

    // =========================================================
    // GET DYNAMIC COMPANY FIELDS
    // ADMIN ONLY
    // =========================================================

    @Transactional(readOnly = true)
    public List<CompanyFieldResponse> getCompanyFields(
            User user) {

        validateAdmin(user);

        String companyId =
                getCompanyId(user);

        List<CompanyField> fields =
                companyFieldRepository
                        .findByCompany_CompanyIdAndActiveTrue(
                                companyId
                        );

        List<CompanyFieldResponse> response =
                new ArrayList<>();

        for (CompanyField field : fields) {

            String value = "";

            CompanyFieldValue fieldValue =
                    companyFieldValueRepository
                            .findFirstByField_FieldId(
                                    field.getFieldId()
                            )
                            .orElse(null);

            if (fieldValue != null) {
                value =
                        fieldValue.getFieldValue();
            }

            response.add(
                    new CompanyFieldResponse(
                            field.getFieldId(),
                            field.getFieldName(),
                            value
                    )
            );
        }

        return response;
    }

    // =========================================================
    // ADD DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    // =========================================================

    @Transactional
    public CompanyFieldResponse addCompanyField(
            User user,
            CompanyFieldRequest request) {

        validateAdmin(user);

        if (request == null) {
            throw new RuntimeException(
                    "Company field information is required."
            );
        }

        String fieldName =
                request.getFieldName() == null
                        ? ""
                        : request.getFieldName().trim();

        String fieldValue =
                request.getFieldValue() == null
                        ? ""
                        : request.getFieldValue().trim();

        if (fieldName.isEmpty()) {
            throw new RuntimeException(
                    "Field Name is required."
            );
        }

        if (fieldValue.isEmpty()) {
            throw new RuntimeException(
                    "Value of Field is required."
            );
        }

        if (fieldName.length() > 100) {
            throw new RuntimeException(
                    "Field Name cannot exceed 100 characters."
            );
        }

        String companyId =
                getCompanyId(user);

        // =====================================================
        // PREVENT DUPLICATE ACTIVE FIELD
        // =====================================================

        boolean alreadyExists =
                companyFieldRepository
                        .existsByCompany_CompanyIdAndFieldNameIgnoreCaseAndActiveTrue(
                                companyId,
                                fieldName
                        );

        if (alreadyExists) {
            throw new RuntimeException(
                    "A company field with this name already exists."
            );
        }

        Company company =
                user.getCompany();

        // =====================================================
        // CREATE FIELD
        // =====================================================

        CompanyField field =
                new CompanyField();

        field.setCompany(company);
        field.setFieldName(fieldName);
        field.setActive(true);

        CompanyField savedField =
                companyFieldRepository.save(field);

        // =====================================================
        // CREATE FIELD VALUE
        // =====================================================

        CompanyFieldValue fieldValueEntity =
                new CompanyFieldValue();

        fieldValueEntity.setField(savedField);
        fieldValueEntity.setFieldValue(fieldValue);

        companyFieldValueRepository.save(
                fieldValueEntity
        );

        return new CompanyFieldResponse(
                savedField.getFieldId(),
                savedField.getFieldName(),
                fieldValue
        );
    }

    // =========================================================
    // UPDATE DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    // =========================================================

    @Transactional
    public CompanyFieldResponse updateCompanyField(
            User user,
            Long fieldId,
            CompanyFieldRequest request) {

        validateAdmin(user);

        if (fieldId == null) {
            throw new RuntimeException(
                    "Field ID is required."
            );
        }

        if (request == null) {
            throw new RuntimeException(
                    "Company field information is required."
            );
        }

        String fieldName =
                request.getFieldName() == null
                        ? ""
                        : request.getFieldName().trim();

        String fieldValue =
                request.getFieldValue() == null
                        ? ""
                        : request.getFieldValue().trim();

        if (fieldName.isEmpty()) {
            throw new RuntimeException(
                    "Field Name is required."
            );
        }

        if (fieldValue.isEmpty()) {
            throw new RuntimeException(
                    "Value of Field is required."
            );
        }

        if (fieldName.length() > 100) {
            throw new RuntimeException(
                    "Field Name cannot exceed 100 characters."
            );
        }

        String companyId =
                getCompanyId(user);

        // =====================================================
        // FIND FIELD
        // =====================================================

        CompanyField field =
                companyFieldRepository
                        .findById(fieldId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company field not found."
                                )
                        );

        // =====================================================
        // SECURITY:
        // FIELD MUST BELONG TO CURRENT COMPANY
        // =====================================================

        if (field.getCompany() == null ||
                field.getCompany().getCompanyId() == null ||
                !companyId.equals(
                        field.getCompany().getCompanyId()
                )) {

            throw new RuntimeException(
                    "You are not allowed to modify this company field."
            );
        }

        if (!field.isActive()) {
            throw new RuntimeException(
                    "This company field is no longer active."
            );
        }

        // =====================================================
        // DUPLICATE NAME CHECK
        // =====================================================

        List<CompanyField> existingFields =
                companyFieldRepository
                        .findByCompany_CompanyIdAndActiveTrue(
                                companyId
                        );

        for (CompanyField existing :
                existingFields) {

            if (existing.getFieldId()
                    .equals(fieldId)) {

                continue;
            }

            if (existing.getFieldName()
                    .equalsIgnoreCase(fieldName)) {

                throw new RuntimeException(
                        "A company field with this name already exists."
                );
            }
        }

        // =====================================================
        // UPDATE FIELD NAME
        // =====================================================

        field.setFieldName(fieldName);

        companyFieldRepository.save(field);

        // =====================================================
        // UPDATE FIELD VALUE
        // =====================================================

        CompanyFieldValue fieldValueEntity =
                companyFieldValueRepository
                        .findFirstByField_FieldId(fieldId)
                        .orElse(null);

        if (fieldValueEntity == null) {

            fieldValueEntity =
                    new CompanyFieldValue();

            fieldValueEntity.setField(field);
        }

        fieldValueEntity.setFieldValue(
                fieldValue
        );

        companyFieldValueRepository.save(
                fieldValueEntity
        );

        return new CompanyFieldResponse(
                field.getFieldId(),
                field.getFieldName(),
                fieldValue
        );
    }

    // =========================================================
    // DELETE DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    //
    // SOFT DELETE:
    // active = false
    // =========================================================

    @Transactional
    public void deleteCompanyField(
            User user,
            Long fieldId) {

        validateAdmin(user);

        if (fieldId == null) {
            throw new RuntimeException(
                    "Field ID is required."
            );
        }

        String companyId =
                getCompanyId(user);

        CompanyField field =
                companyFieldRepository
                        .findById(fieldId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company field not found."
                                )
                        );

        // =====================================================
        // SECURITY:
        // FIELD MUST BELONG TO CURRENT COMPANY
        // =====================================================

        if (field.getCompany() == null ||
                field.getCompany().getCompanyId() == null ||
                !companyId.equals(
                        field.getCompany().getCompanyId()
                )) {

            throw new RuntimeException(
                    "You are not allowed to delete this company field."
            );
        }

        // =====================================================
        // SOFT DELETE
        // =====================================================

        field.setActive(false);

        companyFieldRepository.save(field);
    }

    // =========================================================
    // VALIDATE ADMIN
    // =========================================================

    private void validateAdmin(User user) {

        if (user == null) {
            throw new RuntimeException(
                    "User not found."
            );
        }

        if (user.getRole() == null ||
                !"ADMIN".equals(
                        user.getRole().name()
                )) {

            throw new RuntimeException(
                    "Only ADMIN can manage company fields."
            );
        }

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "No company is assigned to this user."
            );
        }
    }

    // =========================================================
    // GET COMPANY ID
    // =========================================================

    private String getCompanyId(User user) {

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "No company is assigned to this user."
            );
        }

        String companyId =
                user.getCompany().getCompanyId();

        if (companyId == null ||
                companyId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Company ID is missing."
            );
        }

        return companyId;
    }
}