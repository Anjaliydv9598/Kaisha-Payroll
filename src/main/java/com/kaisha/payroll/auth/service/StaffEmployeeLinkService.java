
package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.dto.CreateStaffRequest;
import com.kaisha.payroll.auth.dto.StaffEmployeeResponse;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;

import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

@Service
public class StaffEmployeeLinkService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeFieldRepository employeeFieldRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffEmployeeLinkService(
            EmployeeRepository employeeRepository,
            EmployeeFieldRepository employeeFieldRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeFieldRepository = employeeFieldRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get active employees with their existing details and login status
    @Transactional(readOnly = true)
    public List<StaffEmployeeResponse> getEmployees(String companyId) {

        List<Employee> employees =
                employeeRepository
                        .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                                companyId
                        );

        return employees.stream()
                .map(employee -> {

                    List<EmployeeField> fields =
                            employeeFieldRepository
                                    .findByEmployee_IdOrderByFieldIdAsc(
                                            employee.getId()
                                    );

                    String employeeName = getFieldValue(
                            fields,
                            "fullname",
                            "name",
                            "employeename"
                    );

                    String email = getFieldValue(
                            fields,
                            "email",
                            "emailid",
                            "emailaddress"
                    );

                    String mobileNumber = getFieldValue(
                            fields,
                            "phone",
                            "phonenumber",
                            "mobile",
                            "mobilenumber",
                            "contactnumber"
                    );

                    boolean hasLogin =
                            userRepository.existsByEmployee_Id(
                                    employee.getId()
                            );

                    return new StaffEmployeeResponse(
                            employee.getId(),
                            employee.getEmployeeId(),
                            employeeName,
                            email,
                            mobileNumber,
                            hasLogin
                    );
                })
                .toList();
    }

    // Create login credentials for an existing employee
    @Transactional
    public void createStaffLogin(
            CreateStaffRequest request,
            String companyId
    ) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (companyId == null || companyId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Company ID is required"
            );
        }

        String employeeId = request.getEmployeeId();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (employeeId == null || employeeId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID is required"
            );
        }

        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required"
            );
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Confirm password is required"
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        employeeId = employeeId.trim();

        // Find existing employee within the ADMIN's company
        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        employeeId,
                        companyId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active employee not found in your company"
                ));

        // Prevent duplicate staff login
        if (userRepository.existsByEmployee_Id(employee.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Staff login already exists for this employee"
            );
        }

        // Fetch existing employee details
        List<EmployeeField> fields =
                employeeFieldRepository
                        .findByEmployee_IdOrderByFieldIdAsc(
                                employee.getId()
                        );

        String email = getFieldValue(
                fields,
                "email",
                "emailid",
                "emailaddress"
        );

        String mobileNumber = getFieldValue(
                fields,
                "phone",
                "phonenumber",
                "mobile",
                "mobilenumber",
                "contactnumber"
        );

        // Employee ID automatically becomes username
        String username = employee.getEmployeeId();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists: " + username
            );
        }

        if (email != null
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee email is already used"
            );
        }

        if (mobileNumber != null
                && userRepository.existsByMobileNumber(mobileNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee mobile number is already used"
            );
        }

        // Create User only. Do not create another Employee.
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        user.setPassword(passwordEncoder.encode(password));

        user.setRole(Role.STAFF);
        user.setCompany(employee.getCompany());
        user.setEmployee(employee);
        user.setActive(true);

        userRepository.save(user);
    }

    // Retrieve employee details from EmployeeField
    private String getFieldValue(
            List<EmployeeField> fields,
            String... possibleNames
    ) {

        for (String possibleName : possibleNames) {

            String expectedName = normalize(possibleName);

            for (EmployeeField field : fields) {

                if (field.getFieldName() == null) {
                    continue;
                }

                String actualName = normalize(field.getFieldName());

                if (actualName.equals(expectedName)) {

                    String value = field.getFieldValue();

                    if (value != null && !value.isBlank()) {
                        return value.trim();
                    }
                }
            }
        }

        return null;
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEmployeeDetails(
            Long employeeDbId,
            String companyId
    ) {
        Employee employee = employeeRepository.findById(employeeDbId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"
                ));

        // Ensure employee belongs to the current admin's company.
        if (employee.getCompany() == null ||
                !employee.getCompany().getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view this employee"
            );
        }

        List<EmployeeField> fields =
                employeeFieldRepository
                        .findByEmployee_IdOrderByFieldIdAsc(employee.getId());

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", employee.getId());
        result.put("employeeId", employee.getEmployeeId());

        List<Map<String, String>> employeeDetails = fields.stream()
                .map(field -> {
                    Map<String, String> detail = new LinkedHashMap<>();
                    detail.put("fieldName", field.getFieldName());
                    detail.put("fieldValue", field.getFieldValue());
                    return detail;
                })
                .toList();

        result.put("fields", employeeDetails);

        return result;
    }
}
