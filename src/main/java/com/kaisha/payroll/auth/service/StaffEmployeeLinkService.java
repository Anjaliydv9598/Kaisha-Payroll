
package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.dto.CreateStaffLoginRequest;
import com.kaisha.payroll.auth.dto.StaffEmployeeResponse;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    public List<StaffEmployeeResponse> getEmployees(
            String companyId
    ) {

        List<Employee> employees =
                employeeRepository
                        .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                                companyId
                        );

        return employees.stream()
                .map(employee -> new StaffEmployeeResponse(
                        employee.getId(),
                        employee.getEmployeeId(),
                        userRepository.existsByEmployee_Id(
                                employee.getId()
                        )
                ))
                .toList();
    }

    // Create Staff Login for an existing employee
    @Transactional
    public void createStaffLogin(
            CreateStaffLoginRequest request,
            String companyId
    ) {

        if (request.employeeId() == null ||
                request.employeeId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID is required"
            );
        }

        if (request.username() == null ||
                request.username().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }

        if (request.email() == null ||
                request.email().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required"
            );
        }

        if (request.mobileNumber() == null ||
                request.mobileNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mobile number is required"
            );
        }

        if (request.password() == null ||
                request.password().isBlank() ||
                !request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        // Find employee within the admin's company
        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        request.employeeId(),
                        companyId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active employee not found"
                ));

        // Prevent duplicate Staff Login
        if (userRepository.existsByEmployee_Id(employee.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Staff login already exists"
            );
        }

        // Validate unique login details
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        if (userRepository.existsByMobileNumber(
                request.mobileNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mobile number already exists"
            );
        }

        // Create User account
        User user = new User();

        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setMobileNumber(request.mobileNumber().trim());

        // Never store plain-text passwords
        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setRole(Role.STAFF);
        user.setCompany(employee.getCompany());
        user.setEmployee(employee);
        user.setActive(true);

        userRepository.save(user);
    }
}
