package com.kaisha.payroll.employee.service;

import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeFieldRepository employeeFieldRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeFieldRepository employeeFieldRepository,
            CompanyRepository companyRepository,
            CurrentUserService currentUserService
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeFieldRepository = employeeFieldRepository;
        this.companyRepository = companyRepository;
        this.currentUserService = currentUserService;
    }

    // ============================================================
    // GET ALL ACTIVE EMPLOYEES
    // ============================================================

    @Transactional(readOnly = true)
    public List<Employee> getEmployees() {

        String companyId = currentUserService.getCurrentCompanyId();

        return employeeRepository
                .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                        companyId
                );
    }

    // ============================================================
    // CREATE EMPLOYEE
    // ADMIN ONLY
    // EMPLOYEE ID IS AUTO GENERATED
    // ============================================================

    @Transactional
    public Employee createEmployee() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        Company company =
                companyRepository
                        .findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                )
                        );

        String employeeId =
                generateNextEmployeeId(companyId);

        Employee employee = new Employee();

        employee.setEmployeeId(employeeId);
        employee.setCompany(company);
        employee.setActive(true);

        return employeeRepository.save(employee);
    }

    // ============================================================
    // AUTO GENERATE EMPLOYEE ID
    //
    // E001
    // E002
    // E003
    //
    // Prefix is E by default.
    // Numeric portion is controlled by system.
    // ============================================================

    private String generateNextEmployeeId(String companyId) {

        List<Employee> employees =
                employeeRepository
                        .findByCompany_CompanyIdOrderByEmployeeIdAsc(
                                companyId
                        );

        int highestNumber = 0;

        Pattern pattern =
                Pattern.compile("^[A-Z]+(\\d+)$");

        for (Employee employee : employees) {

            String id = employee.getEmployeeId();

            if (id == null || id.isBlank()) {
                continue;
            }

            Matcher matcher = pattern.matcher(id);

            if (!matcher.matches()) {
                continue;
            }

            try {

                int number =
                        Integer.parseInt(
                                matcher.group(1)
                        );

                highestNumber =
                        Math.max(
                                highestNumber,
                                number
                        );

            } catch (NumberFormatException ignored) {
                // Ignore invalid numeric suffix
            }
        }

        int nextNumber = highestNumber + 1;

        String employeeId =
                String.format(
                        "E%03d",
                        nextNumber
                );

        while (
                employeeRepository
                        .existsByEmployeeIdAndCompany_CompanyId(
                                employeeId,
                                companyId
                        )
        ) {

            nextNumber++;

            employeeId =
                    String.format(
                            "E%03d",
                            nextNumber
                    );
        }

        return employeeId;
    }

    // ============================================================
    // GET EMPLOYEE
    // ============================================================

    @Transactional(readOnly = true)
    public Employee getEmployeeForCompany(
            String employeeId
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return employeeRepository
                .findByEmployeeIdAndCompany_CompanyId(
                        employeeId,
                        companyId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found"
                        )
                );
    }

    // ============================================================
    // GET EMPLOYEE FIELDS
    // ============================================================

    @Transactional(readOnly = true)
    public List<EmployeeField> getEmployeeFields(
            String employeeId
    ) {

        Employee employee =
                getEmployeeForCompany(employeeId);

        return employeeFieldRepository
                .findByEmployee_IdOrderByFieldIdAsc(
                        employee.getId()
                );
    }

    // ============================================================
    // ADMIN ADD FIELD
    // ============================================================

    @Transactional
    public EmployeeField addField(
            String employeeId,
            String fieldName,
            String fieldValue
    ) {

        Employee employee =
                getEmployeeForCompany(employeeId);

        validateField(
                employee,
                fieldName
        );

        EmployeeField field =
                new EmployeeField();

        field.setEmployee(employee);
        field.setFieldName(
                fieldName.trim()
        );
        field.setFieldValue(fieldValue);

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // ADMIN EDIT FIELD
    // ============================================================

    @Transactional
    public EmployeeField updateField(
            String employeeId,
            Long fieldId,
            String fieldName,
            String fieldValue
    ) {

        Employee employee =
                getEmployeeForCompany(employeeId);

        EmployeeField field =
                employeeFieldRepository
                        .findByFieldIdAndEmployee_Id(
                                fieldId,
                                employee.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee field not found"
                                )
                        );

        validateFieldName(
                fieldName
        );

        boolean duplicate =
                employeeFieldRepository
                        .existsByEmployee_IdAndFieldNameIgnoreCaseAndFieldIdNot(
                                employee.getId(),
                                fieldName.trim(),
                                fieldId
                        );

        if (duplicate) {

            throw new RuntimeException(
                    "This field already exists for the employee"
            );
        }

        field.setFieldName(
                fieldName.trim()
        );

        field.setFieldValue(
                fieldValue
        );

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // ADMIN DELETE FIELD
    // ============================================================

    @Transactional
    public void deleteField(
            String employeeId,
            Long fieldId
    ) {

        Employee employee =
                getEmployeeForCompany(employeeId);

        EmployeeField field =
                employeeFieldRepository
                        .findByFieldIdAndEmployee_Id(
                                fieldId,
                                employee.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee field not found"
                                )
                        );

        employeeFieldRepository.delete(field);
    }

    // ============================================================
    // APPROVAL: ADD FIELD
    // ============================================================

    @Transactional
    public EmployeeField addFieldForApproval(
            Employee employee,
            String fieldName,
            String fieldValue
    ) {

        validateField(
                employee,
                fieldName
        );

        EmployeeField field =
                new EmployeeField();

        field.setEmployee(employee);
        field.setFieldName(
                fieldName.trim()
        );
        field.setFieldValue(fieldValue);

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // APPROVAL: EDIT FIELD
    // ============================================================

    @Transactional
    public EmployeeField updateFieldForApproval(
            Employee employee,
            Long fieldId,
            String fieldName,
            String fieldValue
    ) {

        EmployeeField field =
                employeeFieldRepository
                        .findByFieldIdAndEmployee_Id(
                                fieldId,
                                employee.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee field not found"
                                )
                        );

        validateFieldName(fieldName);

        boolean duplicate =
                employeeFieldRepository
                        .existsByEmployee_IdAndFieldNameIgnoreCaseAndFieldIdNot(
                                employee.getId(),
                                fieldName.trim(),
                                fieldId
                        );

        if (duplicate) {

            throw new RuntimeException(
                    "This field already exists for the employee"
            );
        }

        field.setFieldName(
                fieldName.trim()
        );

        field.setFieldValue(
                fieldValue
        );

        return employeeFieldRepository.save(field);
    }

    // ============================================================
    // APPROVAL: DELETE FIELD
    // ============================================================

    @Transactional
    public void deleteFieldForApproval(
            Employee employee,
            Long fieldId
    ) {

        EmployeeField field =
                employeeFieldRepository
                        .findByFieldIdAndEmployee_Id(
                                fieldId,
                                employee.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee field not found"
                                )
                        );

        employeeFieldRepository.delete(field);
    }

    // ============================================================
    // CHANGE EMPLOYEE ID PREFIX
    //
    // E001 -> T001
    //
    // Only prefix changes.
    // Numeric suffix stays unchanged.
    // ============================================================

    @Transactional
    public Employee updateEmployeePrefix(
            String employeeId,
            String newPrefix
    ) {

        Employee employee =
                getEmployeeForCompany(employeeId);

        return updateEmployeePrefixForApproval(
                employee,
                newPrefix
        );
    }

    @Transactional
    public Employee updateEmployeePrefixForApproval(
            Employee employee,
            String newPrefix
    ) {

        validatePrefix(newPrefix);

        String prefix =
                newPrefix
                        .trim()
                        .toUpperCase();

        String currentEmployeeId =
                employee.getEmployeeId();

        String numericPart =
                extractNumericPart(
                        currentEmployeeId
                );

        String newEmployeeId =
                prefix + numericPart;

        String companyId =
                employee
                        .getCompany()
                        .getCompanyId();

        if (
                !newEmployeeId.equals(
                        currentEmployeeId
                )
                        &&
                        employeeRepository
                                .existsByEmployeeIdAndCompany_CompanyId(
                                        newEmployeeId,
                                        companyId
                                )
        ) {

            throw new RuntimeException(
                    "Employee ID " +
                            newEmployeeId +
                            " already exists"
            );
        }

        employee.setEmployeeId(
                newEmployeeId
        );

        return employeeRepository.save(employee);
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateField(
            Employee employee,
            String fieldName
    ) {

        validateFieldName(fieldName);

        boolean exists =
                employeeFieldRepository
                        .existsByEmployee_IdAndFieldNameIgnoreCase(
                                employee.getId(),
                                fieldName.trim()
                        );

        if (exists) {

            throw new RuntimeException(
                    "This field already exists for the employee"
            );
        }
    }

    private void validateFieldName(
            String fieldName
    ) {

        if (
                fieldName == null
                        ||
                        fieldName.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Field name is required"
            );
        }

        if (
                fieldName.trim().length() > 100
        ) {

            throw new RuntimeException(
                    "Field name cannot exceed 100 characters"
            );
        }
    }

    private void validatePrefix(
            String prefix
    ) {

        if (
                prefix == null
                        ||
                        prefix.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Employee ID prefix is required"
            );
        }

        if (
                !prefix
                        .trim()
                        .matches("[A-Za-z]+")
        ) {

            throw new RuntimeException(
                    "Prefix can contain letters only"
            );
        }
    }

    private String extractNumericPart(
            String employeeId
    ) {

        if (
                employeeId == null
                        ||
                        employeeId.isBlank()
        ) {

            throw new RuntimeException(
                    "Invalid employee ID"
            );
        }

        Matcher matcher =
                Pattern
                        .compile("^(.*?)(\\d+)$")
                        .matcher(employeeId);

        if (!matcher.matches()) {

            throw new RuntimeException(
                    "Employee ID does not contain a numeric suffix"
            );
        }

        return matcher.group(2);
    }
}