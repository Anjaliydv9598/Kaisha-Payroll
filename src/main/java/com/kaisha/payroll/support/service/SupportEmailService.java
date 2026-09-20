package com.kaisha.payroll.support.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.support.entity.SupportQuery;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SupportEmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public SupportEmailService(
            JavaMailSender mailSender,
            UserRepository userRepository
    ) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    /*
     * Send new support query to the company's ADMIN email.
     */
    public void sendNewQueryEmailToCompany(
            SupportQuery supportQuery
    ) {

        User admin = userRepository
                .findFirstByCompany_CompanyIdAndRole(
                        supportQuery.getCompanyId(),
                        com.kaisha.payroll.auth.entity.Role.ADMIN
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Company administrator email not found."
                        )
                );

        String companyEmail = admin.getEmail();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(companyEmail);

        message.setSubject(
                "Kaisha Payroll - New Support Query"
        );

        String body =
                "Dear Admin,\n\n" +

                        "A new support query has been submitted " +
                        "through Kaisha Payroll.\n\n" +

                        "Support Query ID: " +
                        supportQuery.getId() +
                        "\n\n" +

                        "Company ID: " +
                        supportQuery.getCompanyId() +
                        "\n" +

                        "Staff / Employee Email: " +
                        supportQuery.getEmail() +
                        "\n\n" +

                        "Query:\n" +
                        "----------------------------------------\n" +
                        supportQuery.getQuery() +
                        "\n" +
                        "----------------------------------------\n\n" +

                        "Submitted At: " +
                        supportQuery.getCreatedAt() +
                        "\n\n" +

                        "Please review this query from the " +
                        "Admin Dashboard.\n\n" +

                        "Regards,\n" +
                        "Kaisha Payroll Support System";

        message.setText(body);

        mailSender.send(message);
    }


    /*
     * Send acknowledgement to employee.
     */
    public void sendQueryReceivedEmail(
            SupportQuery supportQuery
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(
                supportQuery.getEmail()
        );

        message.setSubject(
                "Kaisha Payroll - Support Query Received"
        );

        String body =
                "Dear User,\n\n" +

                        "Thank you for contacting " +
                        "Kaisha Payroll Support.\n\n" +

                        "We have successfully received your " +
                        "support query and forwarded it to " +
                        "the company administrator for review.\n\n" +

                        "Support Query ID: " +
                        supportQuery.getId() +
                        "\n\n" +

                        "Company ID: " +
                        supportQuery.getCompanyId() +
                        "\n\n" +

                        "Your Query:\n" +
                        "----------------------------------------\n" +
                        supportQuery.getQuery() +
                        "\n" +
                        "----------------------------------------\n\n" +

                        "We aim to resolve your query within " +
                        "36 hours.\n\n" +

                        "Thank you for your patience.\n\n" +

                        "Regards,\n" +
                        "Kaisha Payroll Support Team";

        message.setText(body);

        mailSender.send(message);
    }


    /*
     * Send resolution email when ADMIN marks
     * the query as RESOLVED.
     */
    public void sendQueryResolvedEmail(
            SupportQuery supportQuery
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(
                supportQuery.getEmail()
        );

        message.setSubject(
                "Kaisha Payroll - Support Query Resolved"
        );

        String body =
                "Dear User,\n\n" +

                        "Your support query submitted through " +
                        "Kaisha Payroll has been reviewed and " +
                        "marked as resolved by the company administrator.\n\n" +

                        "Support Query ID: " +
                        supportQuery.getId() +
                        "\n\n" +

                        "Company ID: " +
                        supportQuery.getCompanyId() +
                        "\n\n" +

                        "Your Query:\n" +
                        "----------------------------------------\n" +
                        supportQuery.getQuery() +
                        "\n" +
                        "----------------------------------------\n\n" +

                        "Status: RESOLVED\n\n" +

                        "If you continue to experience the issue, " +
                        "please submit a new support query.\n\n" +

                        "Regards,\n" +
                        "Kaisha Payroll Support Team";

        message.setText(body);

        mailSender.send(message);
    }
}