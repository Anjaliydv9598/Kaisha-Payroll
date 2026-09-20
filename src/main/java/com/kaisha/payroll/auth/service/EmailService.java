package com.kaisha.payroll.auth.service;

import com.kaisha.payroll.auth.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // REGISTRATION EMAIL

    public void sendRegistrationEmail(
            User user,
            String companyId,
            String companyName,
            String location,
            String telephoneNumber
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("anjaliydv9598@gmail.com");
        message.setTo(user.getEmail());

        message.setSubject(
                "Kaisha Payroll - Registration Successful"
        );

        String emailBody =
                "Dear Admin,\n\n" +

                        "Your Kaisha Payroll admin account has been created successfully.\n\n" +

                        "Company Details:\n" +
                        "Company ID: " + companyId + "\n" +
                        "Company Name: " + companyName + "\n" +
                        "Location: " + location + "\n" +
                        "Telephone Number: " + telephoneNumber + "\n\n" +

                        "Login Email: " + user.getEmail() + "\n\n" +

                        "You can now log in to your Kaisha Payroll account.\n\n" +

                        "Regards,\n" +
                        "Kaisha Payroll";

        message.setText(emailBody);

        mailSender.send(message);

        System.out.println(
                "REGISTRATION EMAIL -> Email sent successfully to: "
                        + user.getEmail()
        );
    }

    // OTP EMAIL

    public void sendOtpEmail(User user, String otp) {
        sendOtpEmail(
                user.getEmail(),
                otp
        );
    }

    public void sendOtpEmail(
            String email,
            String otp
    ) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("anjaliydv9598@gmail.com");
        message.setTo(email);
        message.setSubject("Kaisha Payroll - Password Reset OTP");

        String emailBody =
                "Dear User,\n\n" +
                        "Your Kaisha Payroll password reset OTP is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for a limited time.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Kaisha Payroll";

        message.setText(emailBody);
        mailSender.send(message);
        System.out.println("OTP EMAIL -> Email sent successfully to: " + email);

    }
}