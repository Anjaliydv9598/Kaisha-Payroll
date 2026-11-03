
package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.dto.RegisterRequest;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CompanyRepository companyRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
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
    // GENERIC CREATE USER METHOD
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
}
