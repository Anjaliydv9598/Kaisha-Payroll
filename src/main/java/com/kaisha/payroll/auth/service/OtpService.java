package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.entity.PasswordResetOtp;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.PasswordResetOtpRepository;
import com.kaisha.payroll.auth.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OtpService {

    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            PasswordResetOtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    // =========================================
    // SEND OTP
    // =========================================

    @Transactional
    public void sendOtp(String identifier) {

        if (identifier == null ||
                identifier.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email address is required."
            );
        }

        String email = identifier.trim().toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email address."
                        )
                );

        if (!user.isActive()) {
            throw new RuntimeException(
                    "Your account is inactive."
            );
        }

        // Remove previous OTP
        otpRepository.deleteByUser(user);

        // Generate 6 digit OTP
        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime expiry =
                now.plusMinutes(10);

        PasswordResetOtp passwordResetOtp =
                new PasswordResetOtp();

        passwordResetOtp.setUser(user);
        passwordResetOtp.setOtp(otp);
        passwordResetOtp.setCreatedAt(now);
        passwordResetOtp.setOtpExpiry(expiry);
        passwordResetOtp.setOtpVerified(false);
        passwordResetOtp.setUsed(false);
        passwordResetOtp.setResetToken(null);

        otpRepository.save(passwordResetOtp);

        // Send OTP to user's email
        emailService.sendOtpEmail(
                user.getEmail(),
                otp
        );
    }


    // =========================================
    // VERIFY OTP
    // =========================================

    @Transactional
    public String verifyOtp(
            String identifier,
            String otp
    ) {

        if (identifier == null ||
                identifier.trim().isEmpty()) {

            throw new RuntimeException(
                    "Email address is required."
            );
        }

        if (otp == null ||
                !otp.matches("\\d{6}")) {

            throw new RuntimeException(
                    "Please enter a valid 6-digit OTP."
            );
        }

        String email =
                identifier.trim().toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with this email address."
                        )
                );

        PasswordResetOtp resetOtp =
                otpRepository
                        .findTopByUserAndOtpOrderByCreatedAtDesc(
                                user,
                                otp
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid OTP."
                                )
                        );

        if (resetOtp.isUsed()) {

            throw new RuntimeException(
                    "This OTP has already been used."
            );
        }

        if (LocalDateTime.now()
                .isAfter(resetOtp.getOtpExpiry())) {

            throw new RuntimeException(
                    "OTP has expired. Please generate a new OTP."
            );
        }

        // Generate secure reset token
        String resetToken =
                UUID.randomUUID().toString();

        resetOtp.setOtpVerified(true);
        resetOtp.setResetToken(resetToken);

        otpRepository.save(resetOtp);

        return resetToken;
    }


    // =========================================
    // RESET PASSWORD
    // =========================================

    @Transactional
    public void resetPassword(
            String resetToken,
            String newPassword,
            String confirmPassword
    ) {

        if (resetToken == null ||
                resetToken.trim().isEmpty()) {

            throw new RuntimeException(
                    "Reset token is missing."
            );
        }

        if (newPassword == null ||
                newPassword.length() < 8) {

            throw new RuntimeException(
                    "Password must contain at least 8 characters."
            );
        }

        if (!newPassword.equals(confirmPassword)) {

            throw new RuntimeException(
                    "Password and confirm password do not match."
            );
        }

        PasswordResetOtp resetOtp =
                otpRepository
                        .findByResetToken(resetToken)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid or expired reset token."
                                )
                        );

        if (!resetOtp.isOtpVerified()) {

            throw new RuntimeException(
                    "OTP has not been verified."
            );
        }

        if (resetOtp.isUsed()) {

            throw new RuntimeException(
                    "This password reset request has already been used."
            );
        }

        if (LocalDateTime.now()
                .isAfter(resetOtp.getOtpExpiry())) {

            throw new RuntimeException(
                    "Password reset request has expired."
            );
        }

        User user = resetOtp.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        // Make reset token unusable
        resetOtp.setUsed(true);

        otpRepository.save(resetOtp);
    }
}