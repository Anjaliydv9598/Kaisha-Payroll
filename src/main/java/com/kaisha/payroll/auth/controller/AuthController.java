package com.kaisha.payroll.auth.controller;

import com.kaisha.payroll.auth.dto.LoginRequest;
import com.kaisha.payroll.auth.dto.LoginResponse;
import com.kaisha.payroll.auth.dto.RegisterRequest;
import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.EmailService;
import com.kaisha.payroll.auth.service.JwtService;
import com.kaisha.payroll.auth.service.OtpService;
import com.kaisha.payroll.auth.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpService otpService;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            OtpService otpService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        try {

            User user = userService.registerUser(request);

            emailService.sendRegistrationEmail(
                    user,
                    request.getCompanyId(),
                    request.getCompanyName(),
                    request.getLocation(),
                    request.getTelephoneNumber()
            );

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "message",
                    "Registration successful. Your admin account has been created and the confirmation email has been sent."
            );

            response.put(
                    "companyId",
                    user.getCompany().getCompanyId()
            );

            response.put(
                    "role",
                    user.getRole().name()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            ex.printStackTrace();

            Map<String, String> response = new HashMap<>();

            response.put(
                    "message",
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }


    // =========================================================
    // ADMIN LOGIN
    // =========================================================

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(
            @RequestBody LoginRequest request
    ) {

        return loginWithRequiredRole(
                request,
                Role.ADMIN
        );
    }


    // =========================================================
    // STAFF LOGIN
    // =========================================================

    @PostMapping("/staff-login")
    public ResponseEntity<?> staffLogin(
            @RequestBody LoginRequest request
    ) {

        return loginWithRequiredRole(
                request,
                Role.STAFF
        );
    }


    // =========================================================
    // COMMON LOGIN
    //
    // Kept for backward compatibility with your existing frontend.
    // Later the frontend will use /admin-login and /staff-login.
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        try {

            User user = userService.findByIdentifier(
                    request.getIdentifier()
            );

            return authenticateUser(user, request);

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid email or password."
                            )
                    );
        }
    }


    // =========================================================
    // ROLE-SPECIFIC LOGIN
    // =========================================================

    private ResponseEntity<?> loginWithRequiredRole(
            LoginRequest request,
            Role requiredRole
    ) {

        try {

            if (request == null ||
                    request.getIdentifier() == null ||
                    request.getIdentifier().trim().isEmpty()) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(
                                Map.of(
                                        "message",
                                        "Email is required."
                                )
                        );
            }

            if (request.getPassword() == null ||
                    request.getPassword().isEmpty()) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(
                                Map.of(
                                        "message",
                                        "Password is required."
                                )
                        );
            }

            User user = userService.findByIdentifier(
                    request.getIdentifier().trim()
            );

            // -------------------------------------------------
            // IMPORTANT:
            // Admin login cannot be used by STAFF.
            // Staff login cannot be used by ADMIN.
            // -------------------------------------------------

            if (user.getRole() != requiredRole) {

                String message;

                if (requiredRole == Role.ADMIN) {

                    message =
                            "This account is not registered as an ADMIN account.";

                } else {

                    message =
                            "This account is not registered as a STAFF account.";
                }

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                Map.of(
                                        "message",
                                        message
                                )
                        );
            }

            return authenticateUser(
                    user,
                    request
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid email or password."
                            )
                    );
        }
    }


    // =========================================================
    // COMMON AUTHENTICATION
    // =========================================================

    private ResponseEntity<?> authenticateUser(
            User user,
            LoginRequest request
    ) {

        // -----------------------------------------------------
        // Check account active/inactive
        // -----------------------------------------------------

        if (!user.isActive()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Your account is inactive."
                            )
                    );
        }


        // -----------------------------------------------------
        // Check password
        // -----------------------------------------------------

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid email or password."
                            )
                    );
        }


        // -----------------------------------------------------
        // Generate JWT
        // -----------------------------------------------------

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name()
        );


        // -----------------------------------------------------
        // Login response
        // -----------------------------------------------------

        return ResponseEntity.ok(
                new LoginResponse(
                        user.getUsername(),
                        user.getRole().name(),
                        token
                )
        );
    }


    // =========================================================
    // FORGOT PASSWORD - SEND OTP
    // =========================================================

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestBody Map<String, String> request
    ) {

        try {

            String identifier =
                    request.get("identifier");

            otpService.sendOtp(identifier);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "OTP has been sent to your email address."
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // FORGOT PASSWORD - VERIFY OTP
    // =========================================================

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> request
    ) {

        try {

            String identifier =
                    request.get("identifier");

            String otp =
                    request.get("otp");

            String resetToken =
                    otpService.verifyOtp(
                            identifier,
                            otp
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "OTP verified successfully.",
                            "resetToken",
                            resetToken
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // FORGOT PASSWORD - RESET
    // =========================================================

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request
    ) {

        try {

            String resetToken =
                    request.get("resetToken");

            String newPassword =
                    request.get("newPassword");

            String confirmPassword =
                    request.get("confirmPassword");

            otpService.resetPassword(
                    resetToken,
                    newPassword,
                    confirmPassword
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Password reset successful."
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }
}