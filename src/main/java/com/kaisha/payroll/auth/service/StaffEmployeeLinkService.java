package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.dto.CreateStaffRequest;
import com.kaisha.payroll.auth.dto.StaffEmployeeResponse;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StaffEmployeeLinkService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffEmployeeLinkService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Fetch active employees and their login status
    @Transactional(readOnly = true)
    public List<StaffEmployeeResponse> getEmployees(String companyId) {

        List<Employee> employees =
                employeeRepository
                        .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                                companyId
                        );

        return employees.stream()
                .map(employee -> new StaffEmployeeResponse(
                        employee.getId(),
                        employee.getEmployeeId(),
                        userRepository.existsByEmployee_Id(employee.getId())
                ))
                .toList();
    }

    // Create Staff Login for an existing employee
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
        String username = request.getUsername();
        String email = request.getEmail();
        String mobileNumber = request.getMobileNumber();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (employeeId == null || employeeId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID is required"
            );
        }

        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mobile number is required"
            );
        }

        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required"
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        employeeId = employeeId.trim();
        username = username.trim();
        email = email.trim();
        mobileNumber = mobileNumber.trim();

        // Find active employee within the ADMIN's company
        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        employeeId,
                        companyId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active employee not found in your company"
                ));

        // Prevent duplicate login for the same employee
        if (userRepository.existsByEmployee_Id(employee.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Staff login already exists for this employee"
            );
        }

        // Validate unique login details
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mobile number already exists"
            );
        }

        // Create User account linked to the existing Employee
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
}