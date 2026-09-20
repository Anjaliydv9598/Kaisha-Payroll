package com.kaisha.payroll.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "password_reset_otp")
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            nullable = false,
            length = 6
    )
    private String otp;

    @Column(
            nullable = false
    )
    private LocalDateTime otpExpiry;

    @Column(
            unique = true,
            length = 100
    )
    private String resetToken;

    @Column(
            nullable = false
    )
    private boolean otpVerified = false;

    @Column(
            nullable = false
    )
    private boolean used = false;

    @Column(
            nullable = false
    )
    private LocalDateTime createdAt;
}