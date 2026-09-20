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

        // ==================================================
        // VALIDATION
        // ==================================================

        if (request == null) {
            throw new RuntimeException(
                    "Registration request is required"
            );
        }


        // ==================================================
        // EMAIL
        // ==================================================

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }


        // ==================================================
        // COMPANY ID
        // ==================================================

        if (request.getCompanyId() == null ||
                request.getCompanyId().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company ID is required"
            );
        }


        // ==================================================
        // PASSWORD
        // ==================================================

        if (request.getPassword() == null ||
                request.getPassword().isEmpty()) {

            throw new RuntimeException(
                    "Password is required"
            );
        }


        // ==================================================
        // CONFIRM PASSWORD
        // ==================================================

        if (request.getConfirmPassword() == null ||
                request.getConfirmPassword().isEmpty()) {

            throw new RuntimeException(
                    "Confirm password is required"
            );
        }


        // ==================================================
        // PASSWORD MATCH
        // ==================================================

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Password and confirm password do not match"
            );
        }


        // ==================================================
        // COMPANY NAME
        // ==================================================

        if (request.getCompanyName() == null ||
                request.getCompanyName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company name is required"
            );
        }


        // ==================================================
        // LOCATION
        // ==================================================

        if (request.getLocation() == null ||
                request.getLocation().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company location is required"
            );
        }


        // ==================================================
        // TELEPHONE NUMBER
        // ==================================================

        if (request.getTelephoneNumber() == null ||
                request.getTelephoneNumber().trim().isEmpty()) {

            throw new RuntimeException(
                    "Telephone number is required"
            );
        }


        // ==================================================
        // CLEAN VALUES
        // ==================================================

        String email =
                request.getEmail().trim();

        String companyId =
                request.getCompanyId().trim();

        String companyName =
                request.getCompanyName().trim();

        String location =
                request.getLocation().trim();

        String telephoneNumber =
                request.getTelephoneNumber().trim();


        // ==================================================
        // CHECK EMAIL
        // ==================================================

        if (userRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "Email is already registered"
            );
        }


        // ==================================================
        // CHECK COMPANY ID
        // ==================================================

        if (companyRepository
                .existsByCompanyId(companyId)) {

            throw new RuntimeException(
                    "Company ID already exists. Please choose another."
            );
        }


        // ==================================================
        // CREATE COMPANY
        // ==================================================

        Company company = new Company();

        company.setCompanyId(companyId);

        company.setCompanyName(companyName);

        company.setAddress(location);

        company.setTelephoneNumber(telephoneNumber);

        company.setActive(true);


        // ==================================================
        // SAVE COMPANY
        // ==================================================

        company = companyRepository.save(company);


        // ==================================================
        // CREATE ADMIN USER
        // ==================================================

        User user = new User();

        /*
         * Email is used as username.
         */

        user.setUsername(email);

        user.setEmail(email);


        // ==================================================
        // PASSWORD SECURITY
        // ==================================================

        /*
         * Never store the plain password.
         */

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // ==================================================
        // ROLE
        // ==================================================

        /*
         * Public registration creates ADMIN only.
         *
         * STAFF users can be created later
         * by an authorized ADMIN.
         */

        user.setRole(Role.ADMIN);


        // ==================================================
        // CONNECT USER TO COMPANY
        // ==================================================

        user.setCompany(company);

        user.setActive(true);


        // ==================================================
        // SAVE ADMIN USER
        // ==================================================

        return userRepository.save(user);
    }


    // ==================================================
    // FIND USER FOR LOGIN
    // ==================================================

    public User findByIdentifier(String identifier) {

        if (identifier == null ||
                identifier.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        String value = identifier.trim();


        // ==================================================
        // FIND BY EMAIL
        // ==================================================

        User user =
                userRepository
                        .findByEmail(value)
                        .orElse(null);

        if (user != null) {
            return user;
        }


        // ==================================================
        // OPTIONAL PHONE LOGIN
        // ==================================================

        /*
         * Kept for compatibility with the existing
         * UserRepository.
         *
         * Normally the frontend uses email login.
         */

        user =
                userRepository
                        .findByMobileNumber(value)
                        .orElse(null);

        if (user != null) {
            return user;
        }


        // ==================================================
        // USER NOT FOUND
        // ==================================================

        throw new RuntimeException(
                "User not found"
        );
    }


    // ==================================================
    // CREATE USER
    // Used later by ADMIN to create STAFF
    // ==================================================

    @Transactional
    public User createUser(
            String username,
            String email,
            String mobileNumber,
            String password,
            Role role,
            String companyId) {

        // ==================================================
        // VALIDATION
        // ==================================================

        if (username == null ||
                username.trim().isEmpty()) {

            throw new RuntimeException(
                    "Username is required"
            );
        }


        if (email == null ||
                email.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }


        if (password == null ||
                password.isEmpty()) {

            throw new RuntimeException(
                    "Password is required"
            );
        }


        if (role == null) {

            throw new RuntimeException(
                    "Role is required"
            );
        }


        if (companyId == null ||
                companyId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Company ID is required"
            );
        }


        // ==================================================
        // CLEAN VALUES
        // ==================================================

        username = username.trim();

        email = email.trim();

        final String cleanCompanyId =
                companyId.trim();

        if (mobileNumber != null) {
            mobileNumber = mobileNumber.trim();
        }


        // ==================================================
        // CHECK USERNAME
        // ==================================================

        if (userRepository
                .existsByUsername(username)) {

            throw new RuntimeException(
                    "Username already exists"
            );
        }


        // ==================================================
        // CHECK EMAIL
        // ==================================================

        if (userRepository
                .existsByEmail(email)) {

            throw new RuntimeException(
                    "Email is already registered"
            );
        }


        // ==================================================
        // CHECK MOBILE NUMBER
        // ==================================================

        if (mobileNumber != null &&
                !mobileNumber.isEmpty() &&
                userRepository
                        .existsByMobileNumber(mobileNumber)) {

            throw new RuntimeException(
                    "Mobile number is already registered"
            );
        }


        // ==================================================
        // FIND COMPANY
        // ==================================================

        Company company =
                companyRepository
                        .findById(cleanCompanyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found with id: "
                                                + cleanCompanyId
                                )
                        );


        // ==================================================
        // CREATE USER
        // ==================================================

        User user = new User();

        user.setUsername(username);

        user.setEmail(email);


        if (mobileNumber != null &&
                !mobileNumber.isEmpty()) {

            user.setMobileNumber(
                    mobileNumber
            );
        }


        // ==================================================
        // HASH PASSWORD
        // ==================================================

        user.setPassword(
                passwordEncoder.encode(password)
        );


        // ==================================================
        // ROLE
        // ==================================================

        user.setRole(role);


        // ==================================================
        // COMPANY
        // ==================================================

        user.setCompany(company);

        user.setActive(true);


        // ==================================================
        // SAVE USER
        // ==================================================

        return userRepository.save(user);
    }
}