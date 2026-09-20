package com.kaisha.payroll.auth.repository;

import com.kaisha.payroll.auth.entity.PasswordResetOtp;
import com.kaisha.payroll.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByUserOrderByCreatedAtDesc(
            User user
    );

    Optional<PasswordResetOtp> findTopByUserAndOtpOrderByCreatedAtDesc(
            User user,
            String otp
    );

    Optional<PasswordResetOtp> findByResetToken(
            String resetToken
    );

    void deleteByUser(User user);
}