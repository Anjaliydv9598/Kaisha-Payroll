
package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get currently authenticated user with Company and Employee
    @Transactional(readOnly = true)
    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String username = authentication.getName();

        if (username == null || username.isBlank()) {
            throw new RuntimeException(
                    "Authenticated username is missing"
            );
        }

        return userRepository
                .findByUsernameWithCompanyAndEmployee(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found: " + username
                        ));
    }

    // Get current company ID
    @Transactional(readOnly = true)
    public String getCurrentCompanyId() {

        User user = getCurrentUser();

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "No company assigned to current user."
            );
        }

        return user.getCompany().getCompanyId();
    }

    // Get current company name
    @Transactional(readOnly = true)
    public String getCurrentCompanyName() {

        User user = getCurrentUser();

        if (user.getCompany() == null) {
            throw new RuntimeException(
                    "User is not linked to a company"
            );
        }

        return user.getCompany().getCompanyName();
    }

    // Get current employee
    @Transactional(readOnly = true)
    public Long getCurrentEmployeeId() {

        User user = getCurrentUser();

        if (user.getEmployee() == null) {
            throw new RuntimeException(
                    "No employee is linked to the current user."
            );
        }

        return user.getEmployee().getId();
    }
}
