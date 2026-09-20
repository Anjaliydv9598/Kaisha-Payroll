package com.kaisha.payroll.support.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.support.dto.SupportQueryRequest;
import com.kaisha.payroll.support.entity.SupportQuery;
import com.kaisha.payroll.support.entity.SupportQueryStatus;
import com.kaisha.payroll.support.repository.SupportQueryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportQueryService {

    private final SupportQueryRepository supportQueryRepository;
    private final UserRepository userRepository;
    private final SupportEmailService supportEmailService;

    public SupportQueryService(
            SupportQueryRepository supportQueryRepository,
            UserRepository userRepository,
            SupportEmailService supportEmailService
    ) {
        this.supportQueryRepository =
                supportQueryRepository;

        this.userRepository =
                userRepository;

        this.supportEmailService =
                supportEmailService;
    }


    /*
     * ------------------------------------------------
     * SUBMIT QUERY
     * ------------------------------------------------
     */
    @Transactional
    public SupportQuery submitQuery(
            SupportQueryRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Support query request is required."
            );
        }

        if (request.getCompanyId() == null ||
                request.getCompanyId().trim().isEmpty()) {

            throw new RuntimeException(
                    "Company ID is required."
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Staff/Employee email is required."
            );
        }

        if (request.getQuery() == null ||
                request.getQuery().trim().isEmpty()) {

            throw new RuntimeException(
                    "Please enter your query."
            );
        }

        String companyId =
                request.getCompanyId().trim();

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String query =
                request.getQuery().trim();


        /*
         * Verify employee/user.
         */
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No registered account found with this email."
                                )
                        );


        if (!user.isActive()) {
            throw new RuntimeException(
                    "Your account is inactive."
            );
        }


        /*
         * Verify company.
         */
        if (user.getCompany() == null ||
                user.getCompany().getCompanyId() == null ||
                !user.getCompany()
                        .getCompanyId()
                        .equalsIgnoreCase(companyId)) {

            throw new RuntimeException(
                    "The email address does not belong to this Company ID."
            );
        }


        /*
         * Create query.
         */
        SupportQuery supportQuery =
                new SupportQuery();

        supportQuery.setCompanyId(
                companyId
        );

        supportQuery.setEmail(
                email
        );

        supportQuery.setQuery(
                query
        );

        supportQuery.setStatus(
                SupportQueryStatus.OPEN
        );

        supportQuery.setCreatedAt(
                LocalDateTime.now()
        );


        SupportQuery saved =
                supportQueryRepository.save(
                        supportQuery
                );


        /*
         * Send email to company admin.
         */
        supportEmailService
                .sendNewQueryEmailToCompany(
                        saved
                );


        /*
         * Send acknowledgement to employee.
         */
        supportEmailService
                .sendQueryReceivedEmail(
                        saved
                );


        return saved;
    }


    /*
     * ------------------------------------------------
     * GET ALL QUERIES
     * ------------------------------------------------
     */
    @Transactional(readOnly = true)
    public List<SupportQuery> getAllQueries() {

        return supportQueryRepository
                .findAllByOrderByCreatedAtDesc();
    }


    /*
     * ------------------------------------------------
     * GET ONE QUERY
     * ------------------------------------------------
     */
    @Transactional(readOnly = true)
    public SupportQuery getQueryById(
            Long id
    ) {

        return supportQueryRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support query not found."
                        )
                );
    }


    /*
     * ------------------------------------------------
     * GET OPEN COUNT
     * ------------------------------------------------
     */
    @Transactional(readOnly = true)
    public long getOpenQueryCount() {

        return supportQueryRepository
                .countByStatus(
                        SupportQueryStatus.OPEN
                );
    }


    /*
     * ------------------------------------------------
     * UPDATE STATUS
     * ------------------------------------------------
     */
    @Transactional
    public SupportQuery updateStatus(
            Long id,
            SupportQueryStatus newStatus
    ) {

        if (newStatus == null) {
            throw new RuntimeException(
                    "Status is required."
            );
        }

        SupportQuery supportQuery =
                getQueryById(id);

        SupportQueryStatus oldStatus =
                supportQuery.getStatus();


        supportQuery.setStatus(
                newStatus
        );


        /*
         * RESOLVED or CLOSED = resolvedAt.
         */
        if (newStatus ==
                SupportQueryStatus.RESOLVED ||
                newStatus ==
                        SupportQueryStatus.CLOSED) {

            if (supportQuery.getResolvedAt()
                    == null) {

                supportQuery.setResolvedAt(
                        LocalDateTime.now()
                );
            }

        } else {

            supportQuery.setResolvedAt(null);
        }


        SupportQuery saved =
                supportQueryRepository.save(
                        supportQuery
                );


        /*
         * Email employee ONLY when it changes
         * to RESOLVED.
         */
        if (newStatus ==
                SupportQueryStatus.RESOLVED &&
                oldStatus !=
                        SupportQueryStatus.RESOLVED) {

            supportEmailService
                    .sendQueryResolvedEmail(
                            saved
                    );
        }


        return saved;
    }
}