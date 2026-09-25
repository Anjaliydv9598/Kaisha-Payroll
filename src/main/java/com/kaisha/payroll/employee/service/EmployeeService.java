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

import java.util.List;
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
            EmployeeNumberSeriesService numberSeriesService
    ) {

        this.employeeRepository =
                employeeRepository;

        this.employeeFieldRepository =
                employeeFieldRepository;

        this.companyRepository =
                companyRepository;

        this.currentUserService =
                currentUserService;

        this.numberSeriesService =
                numberSeriesService;
    }

    // ============================================================
    // GET ALL ACTIVE EMPLOYEES
    // ADMIN + STAFF
    // ============================================================

    @Transactional(readOnly = true)
    public List<Employee> getEmployees() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return employeeRepository
                .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                        companyId
                );
    }

    // ============================================================
    // CREATE EMPLOYEE
    // ADMIN ONLY
    //
    // Department is required because the employee ID is generated
    // from the department's configured Number Series.
    //
    // Example:
    //
    // HR     -> HR001
    // Finance -> FIN001
    // CO     -> CO001
    // PP     -> PP001
    // ============================================================

    @Transactional
    public Employee createEmployee(
            String department
    ) {

        if (
                department == null
                        ||
                        department.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Department is required to create an employee"
            );
        }

        String cleanDepartment =
                department
                        .trim()
                        .toUpperCase();

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

        /*
         * Number Series Service generates the employee ID
         * according to the configured department series.
         */
        String employeeId =
                numberSeriesService
                        .generateNextEmployeeId(
                                cleanDepartment
                        );

        Employee employee =
                new Employee();

        employee.setEmployeeId(
                employeeId
        );

        employee.setCompany(
                company
        );

        employee.setActive(
                true
        );

        /*
         * Store department as an EmployeeField.
         *
         * Your current application already stores employee
         * details through EmployeeField.
         */
        EmployeeField departmentField =
                new EmployeeField();

        departmentField.setEmployee(
                employee
        );

        departmentField.setFieldName(
                "Department"
        );

        departmentField.setFieldValue(
                cleanDepartment
        );

        Employee savedEmployee =
                employeeRepository.save(
                        employee
                );

        employeeFieldRepository.save(
                departmentField
        );

        return savedEmployee;
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
                getEmployeeForCompany(
                        employeeId
                );

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
                getEmployeeForCompany(
                        employeeId
                );

        validateField(
                employee,
                fieldName
        );

        EmployeeField field =
                new EmployeeField();

        field.setEmployee(
                employee
        );

        field.setFieldName(
                fieldName.trim()
        );

        field.setFieldValue(
                fieldValue
        );

        return employeeFieldRepository.save(
                field
        );
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
                getEmployeeForCompany(
                        employeeId
                );

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

        return employeeFieldRepository.save(
                field
        );
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
                getEmployeeForCompany(
                        employeeId
                );

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

        employeeFieldRepository.delete(
                field
        );
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

        field.setEmployee(
                employee
        );

        field.setFieldName(
                fieldName.trim()
        );

        field.setFieldValue(
                fieldValue
        );

        return employeeFieldRepository.save(
                field
        );
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

        return employeeFieldRepository.save(
                field
        );
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

        employeeFieldRepository.delete(
                field
        );
    }

    // ============================================================
    // CHANGE EMPLOYEE ID PREFIX
    //
    // Example:
    //
    // HR001 -> STAFF001
    //
    // Numeric suffix remains unchanged.
    // ============================================================

    @Transactional
    public Employee updateEmployeePrefix(
            String employeeId,
            String newPrefix
    ) {

        Employee employee =
                getEmployeeForCompany(
                        employeeId
                );

        return updateEmployeePrefixForApproval(
                employee,
                newPrefix
        );
    }

    // ============================================================
    // CHANGE PREFIX FOR APPROVAL
    // ============================================================

    @Transactional
    public Employee updateEmployeePrefixForApproval(
            Employee employee,
            String newPrefix
    ) {

        validatePrefix(
                newPrefix
        );

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
                prefix +
                        numericPart;

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

        return employeeRepository.save(
                employee
        );
    }

    // ============================================================
    // VALIDATE EMPLOYEE FIELD
    // ============================================================

    private void validateField(
            Employee employee,
            String fieldName
    ) {

        validateFieldName(
                fieldName
        );

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

    // ============================================================
    // VALIDATE FIELD NAME
    // ============================================================

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

    // ============================================================
    // VALIDATE PREFIX
    // ============================================================

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

    // ============================================================
    // EXTRACT NUMERIC PART
    //
    // HR001 -> 001
    // FIN025 -> 025
    // ============================================================

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
                        .matcher(
                                employeeId
                        );

        if (!matcher.matches()) {

            throw new RuntimeException(
                    "Employee ID does not contain a numeric suffix"
            );
        }

        return matcher.group(2);
    }
}