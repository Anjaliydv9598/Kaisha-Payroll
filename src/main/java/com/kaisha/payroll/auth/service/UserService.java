package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.dto.CreateStaffRequest;
import com.kaisha.payroll.auth.dto.RegisterRequest;
import com.kaisha.payroll.auth.dto.StaffEmployeeResponse;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
    }

    // ==================================================
    // REGISTER COMPANY + ADMIN
    // ==================================================

    @Transactional
    public User registerUser(RegisterRequest request) {

        if (request == null) {
            throw new RuntimeException("Registration request is required");
        }

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getCompanyId() == null ||
                request.getCompanyId().trim().isEmpty()) {
            throw new RuntimeException("Company ID is required");
        }

        if (request.getPassword() == null ||
                request.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (request.getConfirmPassword() == null ||
                request.getConfirmPassword().isEmpty()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "Password and confirm password do not match");
        }

        if (request.getCompanyName() == null ||
                request.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }

        if (request.getLocation() == null ||
                request.getLocation().trim().isEmpty()) {
            throw new RuntimeException("Company location is required");
        }

        if (request.getTelephoneNumber() == null ||
                request.getTelephoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Telephone number is required");
        }

        String email = request.getEmail().trim();
        String companyId = request.getCompanyId().trim();
        String companyName = request.getCompanyName().trim();
        String location = request.getLocation().trim();
        String telephoneNumber = request.getTelephoneNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered");
        }

        if (companyRepository.existsByCompanyId(companyId)) {
            throw new RuntimeException(
                    "Company ID already exists. Please choose another.");
        }

        Company company = new Company();
        company.setCompanyId(companyId);
        company.setCompanyName(companyName);
        company.setAddress(location);
        company.setTelephoneNumber(telephoneNumber);
        company.setActive(true);

        company = companyRepository.save(company);

        User user = new User();

        // Email is used as username.
        user.setUsername(email);
        user.setEmail(email);

        // Store encoded password only.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Public registration creates ADMIN only.
        user.setRole(Role.ADMIN);

        user.setCompany(company);
        user.setActive(true);

        // Admin does not need an Employee relationship.
        user.setEmployee(null);

        return userRepository.save(user);
    }

    // ==================================================
    // FIND USER FOR LOGIN
    // ==================================================

    public User findByIdentifier(String identifier) {

        if (identifier == null || identifier.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        String value = identifier.trim();

        User user = userRepository
                .findByEmail(value)
                .orElse(null);

        if (user != null) {
            return user;
        }

        // Optional phone login.
        user = userRepository
                .findByMobileNumber(value)
                .orElse(null);

        if (user != null) {
            return user;
        }

        throw new RuntimeException("User not found");
    }

    // ==================================================
    // EXISTING GENERIC CREATE USER METHOD
    // ==================================================

    @Transactional
    public User createUser(
            String username,
            String email,
            String mobileNumber,
            String password,
            Role role,
            String companyId) {

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (role == null) {
            throw new RuntimeException("Role is required");
        }

        if (companyId == null || companyId.trim().isEmpty()) {
            throw new RuntimeException("Company ID is required");
        }

        username = username.trim();
        email = email.trim();

        final String cleanCompanyId = companyId.trim();

        if (mobileNumber != null) {
            mobileNumber = mobileNumber.trim();
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered");
        }

        if (mobileNumber != null &&
                !mobileNumber.isEmpty() &&
                userRepository.existsByMobileNumber(mobileNumber)) {
            throw new RuntimeException(
                    "Mobile number is already registered");
        }

        Company company = companyRepository
                .findById(cleanCompanyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Company not found with id: " + cleanCompanyId));

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);

        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            user.setMobileNumber(mobileNumber);
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCompany(company);
        user.setActive(true);

        return userRepository.save(user);
    }

    // ==================================================
    // GET ACTIVE EMPLOYEES FOR CURRENT ADMIN'S COMPANY
    // ==================================================

    @Transactional(readOnly = true)
    public List<StaffEmployeeResponse> getStaffEmployees() {

        User currentAdmin = currentUserService.getCurrentUser();

        validateAdmin(currentAdmin);

        String companyId = currentAdmin.getCompany().getCompanyId();

        List<Employee> employees = employeeRepository
                .findByCompany_CompanyIdAndActiveTrueOrderByEmployeeIdAsc(
                        companyId);

        return employees.stream()
                .map(employee -> new StaffEmployeeResponse(
                        employee.getId(),
                        employee.getEmployeeId(),
                        userRepository.existsByEmployee_Id(employee.getId())
                ))
                .toList();
    }

    // ==================================================
    // CREATE STAFF LOGIN AND LINK EXISTING EMPLOYEE
    // ==================================================

    @Transactional
    public User createStaffAccount(CreateStaffRequest request) {

        User currentAdmin = currentUserService.getCurrentUser();

        validateAdmin(currentAdmin);

        if (request == null) {
            throw new RuntimeException(
                    "Staff creation request is required");
        }

        String employeeId = cleanRequired(
                request.getEmployeeId(), "Employee ID is required");

        String username = cleanRequired(
                request.getUsername(), "Username is required");

        String email = cleanRequired(
                request.getEmail(), "Email is required");

        String mobileNumber = cleanRequired(
                request.getMobileNumber(), "Mobile number is required");

        String password = request.getPassword();

        String confirmPassword = request.getConfirmPassword();

        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Confirm password is required");
        }

        if (!password.equals(confirmPassword)) {
            throw new RuntimeException(
                    "Password and confirm password do not match");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email is already registered");
        }

        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new RuntimeException(
                    "Mobile number is already registered");
        }

        Company company = currentAdmin.getCompany();

        String companyId = company.getCompanyId();

        // Find employee only inside the logged-in admin's company.
        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        employeeId, companyId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Active employee not found in your company: "
                                        + employeeId));

        // An employee can have only one staff login.
        if (userRepository.existsByEmployee_Id(employee.getId())) {
            throw new RuntimeException(
                    "This employee already has a staff login");
        }

        User staff = new User();

        staff.setUsername(username);
        staff.setEmail(email);
        staff.setMobileNumber(mobileNumber);
        staff.setPassword(passwordEncoder.encode(password));
        staff.setRole(Role.STAFF);
        staff.setCompany(company);
        staff.setEmployee(employee);
        staff.setActive(true);

        return userRepository.save(staff);
    }

    // ==================================================
    // ADMIN VALIDATION
    // ==================================================

    private void validateAdmin(User user) {

        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException(
                    "Only ADMIN users can manage staff accounts");
        }

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "Your admin account is not linked to a company");
        }
    }

    // ==================================================
    // REQUIRED STRING VALIDATION
    // ==================================================

    private String cleanRequired(String value, String message) {

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }

        return value.trim();
    }
}