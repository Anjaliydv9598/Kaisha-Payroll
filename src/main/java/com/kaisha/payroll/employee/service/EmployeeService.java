package com.kaisha.payroll.employee.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.numberseries.service.EmployeeNumberSeriesService;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeFieldRepository employeeFieldRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;
    private final EmployeeNumberSeriesService numberSeriesService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeFieldRepository employeeFieldRepository,
            CompanyRepository companyRepository,
            CurrentUserService currentUserService,
            EmployeeNumberSeriesService numberSeriesService) {

        this.employeeRepository = employeeRepository;
        this.employeeFieldRepository = employeeFieldRepository;
        this.companyRepository = companyRepository;
        this.currentUserService = currentUserService;
        this.numberSeriesService = numberSeriesService;
    }

    // ============================================================
    // GET ALL ACTIVE EMPLOYEES
    // ============================================================

    @Transactional(readOnly = true)
    public List<Employee> getEmployees() {

        String companyId = currentUserService.getCurrentCompanyId();

        return employeeRepository
                .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                        companyId);
    }

    // ============================================================
    // GET EMPLOYEE RECORDS WITH THEIR FIELD VALUES
    // ============================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmployeeRecords() {

        List<Employee> employees = getEmployees();

        List<Map<String, Object>> records = new ArrayList<>();

        for (Employee employee : employees) {

            List<EmployeeField> fields =
                    employeeFieldRepository
                            .findByEmployee_IdOrderByFieldIdAsc(
                                    employee.getId());

            Map<String, Object> record = new LinkedHashMap<>();

            record.put("employeeId", employee.getEmployeeId());
            record.put("active", employee.isActive());
            record.put("detailsCompleted", employee.isDetailsCompleted());

            record.put("department", getFieldValue(fields, "Department"));
            record.put("fullName", getFieldValue(fields, "Full Name"));
            record.put("email", getFieldValue(fields, "Email"));
            record.put("phone", getFieldValue(fields, "Phone"));
            record.put("designation", getFieldValue(fields, "Designation"));
            record.put("joiningDate", getFieldValue(fields, "Joining Date"));
            record.put("salary", getFieldValue(fields, "Salary"));

            records.add(record);
        }

        return records;
    }

    // ============================================================
    // LEGACY CREATE EMPLOYEE METHOD
    // Kept to avoid removing the existing service method.
    // The new Employee Data Create screen will use reserveEmployeeId.
    // ============================================================

    @Transactional
    public Employee createEmployee(String department) {

        if (department == null || department.trim().isEmpty()) {
            throw new RuntimeException(
                    "Department is required to create an employee");
        }

        String cleanDepartment = department.trim().toUpperCase();
        String companyId = currentUserService.getCurrentCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        String employeeId =
                numberSeriesService.generateNextEmployeeId(cleanDepartment);

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setCompany(company);
        employee.setActive(true);

        Employee savedEmployee = employeeRepository.save(employee);

        EmployeeField departmentField = new EmployeeField();
        departmentField.setEmployee(savedEmployee);
        departmentField.setFieldName("Department");
        departmentField.setFieldValue(cleanDepartment);

        employeeFieldRepository.save(departmentField);

        return savedEmployee;
    }

    // ============================================================
    // RESERVE AN EXISTING EMPLOYEE ID
    // Does not create a new Employee row or generate a new ID.
    // ============================================================

    @Transactional(readOnly = true)
    public Employee reserveEmployeeId(String department) {

        if (department == null || department.trim().isEmpty()) {
            throw new RuntimeException("Please select a department");
        }

        String companyId = currentUserService.getCurrentCompanyId();
        String selectedDepartment = department.trim();

        List<Employee> pendingEmployees =
                employeeRepository
                        .findByCompany_CompanyIdAndDetailsCompletedFalseOrderByEmployeeIdAsc(
                                companyId);

        for (Employee employee : pendingEmployees) {

            if (!employee.isActive()) {
                continue;
            }

            List<EmployeeField> fields =
                    employeeFieldRepository
                            .findByEmployee_IdOrderByFieldIdAsc(
                                    employee.getId());

            String existingDepartment =
                    getFieldValue(fields, "Department");

            if (existingDepartment != null
                    && existingDepartment.trim()
                    .equalsIgnoreCase(selectedDepartment)) {

                return employee;
            }
        }

        throw new RuntimeException(
                "No reserved Employee IDs are available for department: "
                        + selectedDepartment);
    }

    // ============================================================
    // GET ONE EMPLOYEE FOR THE CURRENT COMPANY
    // ============================================================

    @Transactional(readOnly = true)
    public Employee getEmployeeForCompany(String employeeId) {

        String companyId = currentUserService.getCurrentCompanyId();

        return employeeRepository
                .findByEmployeeIdAndCompany_CompanyId(
                        employeeId, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));
    }

    // ============================================================
    // GET EMPLOYEE FIELDS
    // ============================================================

    @Transactional(readOnly = true)
    public List<EmployeeField> getEmployeeFields(String employeeId) {

        Employee employee = getEmployeeForCompany(employeeId);

        return employeeFieldRepository
                .findByEmployee_IdOrderByFieldIdAsc(employee.getId());
    }

    // ============================================================
    // ADMIN: ADD FIELD
    // ============================================================

    @Transactional
    public EmployeeField addField(
            String employeeId,
            String fieldName,
            String fieldValue) {

        Employee employee = getEmployeeForCompany(employeeId);

        validateField(employee, fieldName);
        validateFieldValue(fieldName, fieldValue);

        EmployeeField field = new EmployeeField();
        field.setEmployee(employee);
        field.setFieldName(fieldName.trim());
        field.setFieldValue(fieldValue.trim());

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // ADMIN: UPDATE FIELD
    // ============================================================

    @Transactional
    public EmployeeField updateField(
            String employeeId,
            Long fieldId,
            String fieldName,
            String fieldValue) {

        Employee employee = getEmployeeForCompany(employeeId);

        EmployeeField field = employeeFieldRepository
                .findByFieldIdAndEmployee_Id(fieldId, employee.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee field not found"));

        validateFieldName(fieldName);
        validateFieldValue(fieldName, fieldValue);
        validateNoDuplicateField(employee, fieldName, fieldId);

        field.setFieldName(fieldName.trim());
        field.setFieldValue(fieldValue.trim());

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // ADMIN: DELETE FIELD
    // ============================================================

    @Transactional
    public void deleteField(String employeeId, Long fieldId) {

        Employee employee = getEmployeeForCompany(employeeId);

        EmployeeField field = employeeFieldRepository
                .findByFieldIdAndEmployee_Id(fieldId, employee.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee field not found"));

        employeeFieldRepository.delete(field);
    }

    // ============================================================
    // APPROVAL: ADD FIELD
    // ============================================================

    @Transactional
    public EmployeeField addFieldForApproval(
            Employee employee,
            String fieldName,
            String fieldValue) {

        validateField(employee, fieldName);
        validateFieldValue(fieldName, fieldValue);

        EmployeeField field = new EmployeeField();
        field.setEmployee(employee);
        field.setFieldName(fieldName.trim());
        field.setFieldValue(fieldValue.trim());

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // APPROVAL: UPDATE FIELD
    // ============================================================

    @Transactional
    public EmployeeField updateFieldForApproval(
            Employee employee,
            Long fieldId,
            String fieldName,
            String fieldValue) {

        EmployeeField field = employeeFieldRepository
                .findByFieldIdAndEmployee_Id(fieldId, employee.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee field not found"));

        validateFieldName(fieldName);
        validateFieldValue(fieldName, fieldValue);
        validateNoDuplicateField(employee, fieldName, fieldId);

        field.setFieldName(fieldName.trim());
        field.setFieldValue(fieldValue.trim());

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // APPROVAL: DELETE FIELD
    // ============================================================

    @Transactional
    public void deleteFieldForApproval(
            Employee employee,
            Long fieldId) {

        EmployeeField field = employeeFieldRepository
                .findByFieldIdAndEmployee_Id(fieldId, employee.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee field not found"));

        employeeFieldRepository.delete(field);
    }

    // ============================================================
    // CHANGE EMPLOYEE ID PREFIX
    // ============================================================

    @Transactional
    public Employee updateEmployeePrefix(
            String employeeId,
            String newPrefix) {

        Employee employee = getEmployeeForCompany(employeeId);

        return updateEmployeePrefixForApproval(employee, newPrefix);
    }

    // ============================================================
    // CHANGE PREFIX FOR APPROVAL
    // ============================================================

    @Transactional
    public Employee updateEmployeePrefixForApproval(
            Employee employee,
            String newPrefix) {

        validatePrefix(newPrefix);

        String prefix = newPrefix.trim().toUpperCase();
        String currentEmployeeId = employee.getEmployeeId();
        String numericPart = extractNumericPart(currentEmployeeId);
        String newEmployeeId = prefix + numericPart;

        String companyId = employee.getCompany().getCompanyId();

        if (!newEmployeeId.equals(currentEmployeeId)
                && employeeRepository
                .existsByEmployeeIdAndCompany_CompanyId(
                        newEmployeeId, companyId)) {

            throw new RuntimeException(
                    "Employee ID " + newEmployeeId + " already exists");
        }

        employee.setEmployeeId(newEmployeeId);

        return employeeRepository.save(employee);
    }

    // ============================================================
    // COMPLETE A RESERVED EMPLOYEE
    // Updates the existing reserved Employee row.
    // ============================================================

    @Transactional
    public Employee completeReservedEmployee(
            String employeeId,
            Map<String, Object> details) {

        Employee employee = getEmployeeForCompany(employeeId);

        if (employee.isDetailsCompleted()) {
            throw new RuntimeException(
                    "Employee details have already been completed");
        }

        saveEmployeeDetails(employee, details);

        employee.setDetailsCompleted(true);

        return employeeRepository.save(employee);
    }

    // ============================================================
    // EDIT AN EXISTING COMPLETED EMPLOYEE
    // Employee ID and Department cannot be changed here.
    // ============================================================

    @Transactional
    public Employee updateEmployeeDetails(
            String employeeId,
            Map<String, Object> details) {

        Employee employee = getEmployeeForCompany(employeeId);

        if (!employee.isDetailsCompleted()) {
            throw new RuntimeException(
                    "Complete the employee details before editing");
        }

        saveEmployeeDetails(employee, details);

        return employeeRepository.save(employee);
    }

    // ============================================================
    // SAVE OR UPDATE EMPLOYEE DETAILS
    // ============================================================

    private void saveEmployeeDetails(
            Employee employee,
            Map<String, Object> details) {

        if (details == null || details.isEmpty()) {
            throw new RuntimeException("Employee details are required");
        }

        Map<String, String> allowedFields = new LinkedHashMap<>();

        allowedFields.put("fullname", "Full Name");
        allowedFields.put("email", "Email");
        allowedFields.put("phone", "Phone");
        allowedFields.put("designation", "Designation");
        allowedFields.put("joiningdate", "Joining Date");
        allowedFields.put("salary", "Salary");

        Map<String, String> normalizedDetails = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : details.entrySet()) {

            if (entry.getKey() == null) {
                continue;
            }

            String key = entry.getKey()
                    .replaceAll("[^A-Za-z]", "")
                    .toLowerCase(Locale.ROOT);

            String fieldName = allowedFields.get(key);

            if (fieldName == null) {
                throw new RuntimeException(
                        "Unsupported employee field: " + entry.getKey()
                                + ". Employee ID and Department cannot be changed here.");
            }

            String value = entry.getValue() == null
                    ? ""
                    : String.valueOf(entry.getValue()).trim();

            normalizedDetails.put(fieldName, value);
        }

        // Required fields
        validateFieldValue(
                "Full Name", normalizedDetails.get("Full Name"));

        validateFieldValue(
                "Email", normalizedDetails.get("Email"));

        validateFieldValue(
                "Phone", normalizedDetails.get("Phone"));

        // Existing fields for this employee
        List<EmployeeField> existingFields =
                employeeFieldRepository
                        .findByEmployee_IdOrderByFieldIdAsc(
                                employee.getId());

        for (Map.Entry<String, String> entry
                : normalizedDetails.entrySet()) {

            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            EmployeeField existingField = null;

            for (EmployeeField field : existingFields) {
                if (field.getFieldName() != null
                        && field.getFieldName()
                        .equalsIgnoreCase(fieldName)) {

                    existingField = field;
                    break;
                }
            }

            // Optional fields can be left empty.
            // If an existing optional value is cleared, save it as null.
            if (fieldValue == null || fieldValue.isBlank()) {

                if (existingField != null
                        && !isRequiredField(fieldName)) {
                    existingField.setFieldValue(null);
                    employeeFieldRepository.save(existingField);
                }

                continue;
            }

            if (existingField != null) {

                existingField.setFieldValue(fieldValue);
                employeeFieldRepository.save(existingField);

            } else {

                EmployeeField newField = new EmployeeField();
                newField.setEmployee(employee);
                newField.setFieldName(fieldName);
                newField.setFieldValue(fieldValue);

                employeeFieldRepository.save(newField);
            }
        }
    }

    private boolean isRequiredField(String fieldName) {

        return "Full Name".equalsIgnoreCase(fieldName)
                || "Email".equalsIgnoreCase(fieldName)
                || "Phone".equalsIgnoreCase(fieldName);
    }

    // ============================================================
    // GET A FIELD VALUE BY FIELD NAME
    // ============================================================

    private String getFieldValue(
            List<EmployeeField> fields,
            String fieldName) {

        for (EmployeeField field : fields) {

            if (field.getFieldName() != null
                    && field.getFieldName()
                    .equalsIgnoreCase(fieldName)) {

                return field.getFieldValue();
            }
        }

        return null;
    }

    // ============================================================
    // VALIDATE FIELD NAME AND CHECK DUPLICATES
    // ============================================================

    private void validateField(
            Employee employee,
            String fieldName) {

        validateFieldName(fieldName);

        boolean exists = employeeFieldRepository
                .existsByEmployee_IdAndFieldNameIgnoreCase(
                        employee.getId(), fieldName.trim());

        if (exists) {
            throw new RuntimeException(
                    "This field already exists for the employee: "
                            + fieldName);
        }
    }

    private void validateNoDuplicateField(
            Employee employee,
            String fieldName,
            Long fieldId) {

        boolean duplicate = employeeFieldRepository
                .existsByEmployee_IdAndFieldNameIgnoreCaseAndFieldIdNot(
                        employee.getId(), fieldName.trim(), fieldId);

        if (duplicate) {
            throw new RuntimeException(
                    "This field already exists for the employee: "
                            + fieldName);
        }
    }

    private void validateFieldName(String fieldName) {

        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new RuntimeException("Field name is required");
        }

        if (fieldName.trim().length() > 100) {
            throw new RuntimeException(
                    "Field name cannot exceed 100 characters");
        }
    }

    private void validateFieldValue(
            String fieldName,
            String fieldValue) {

        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            throw new RuntimeException(
                    "Value is required for field: " + fieldName);
        }
    }

    private void validatePrefix(String prefix) {

        if (prefix == null || prefix.trim().isEmpty()) {
            throw new RuntimeException(
                    "Employee ID prefix is required");
        }

        if (!prefix.trim().matches("[A-Za-z]+")) {
            throw new RuntimeException(
                    "Prefix can contain letters only");
        }
    }

    // HR001 -> 001; FIN025 -> 025
    private String extractNumericPart(String employeeId) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new RuntimeException("Invalid employee ID");
        }

        Matcher matcher = Pattern
                .compile("^(.*?)(\\d+)$")
                .matcher(employeeId);

        if (!matcher.matches()) {
            throw new RuntimeException(
                    "Employee ID does not contain a numeric suffix");
        }

        return matcher.group(2);
    }
}