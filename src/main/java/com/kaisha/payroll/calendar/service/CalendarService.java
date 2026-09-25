package com.kaisha.payroll.calendar.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.calendar.dto.CalendarEventRequest;
import com.kaisha.payroll.calendar.dto.CalendarEventResponse;
import com.kaisha.payroll.calendar.entity.CalendarEvent;
import com.kaisha.payroll.calendar.repository.CalendarEventRepository;
import com.kaisha.payroll.company.entity.Company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarService(
            CalendarEventRepository calendarEventRepository
    ) {
        this.calendarEventRepository =
                calendarEventRepository;
    }

    // =====================================================
    // GET ALL EVENTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getAllEvents(
            User user
    ) {

        Company company = getCompany(user);

        String companyId =
                getCompanyId(company);

        return calendarEventRepository
                .findByCompany_CompanyIdOrderByEventDateAsc(
                        companyId
                )
                .stream()
                .map(CalendarEventResponse::new)
                .toList();
    }

    // =====================================================
    // GET EVENTS BY YEAR
    // =====================================================

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getEventsByYear(
            User user,
            int year
    ) {

        if (year < 1900 || year > 2500) {

            throw new RuntimeException(
                    "Invalid calendar year."
            );
        }

        Company company = getCompany(user);

        String companyId =
                getCompanyId(company);

        LocalDate startDate =
                LocalDate.of(
                        year,
                        1,
                        1
                );

        LocalDate endDate =
                LocalDate.of(
                        year,
                        12,
                        31
                );

        return calendarEventRepository
                .findByCompany_CompanyIdAndEventDateBetweenOrderByEventDateAsc(
                        companyId,
                        startDate,
                        endDate
                )
                .stream()
                .map(CalendarEventResponse::new)
                .toList();
    }

    // =====================================================
    // GET ONE EVENT
    // =====================================================

    @Transactional(readOnly = true)
    public CalendarEventResponse getEventById(
            User user,
            Long eventId
    ) {

        if (eventId == null) {

            throw new RuntimeException(
                    "Calendar event ID is required."
            );
        }

        Company company = getCompany(user);

        String companyId =
                getCompanyId(company);

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompany_CompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Calendar event not found."
                                )
                        );

        return new CalendarEventResponse(event);
    }

    // =====================================================
    // CREATE EVENT
    // ADMIN ONLY
    // =====================================================

    @Transactional
    public CalendarEventResponse createEvent(
            User user,
            CalendarEventRequest request
    ) {

        validateAdmin(user);

        validateRequest(request);

        Company company =
                getCompany(user);

        CalendarEvent event =
                new CalendarEvent();

        /*
         * Connect the event to the existing
         * Company entity.
         */
        event.setCompany(company);

        event.setEventDate(
                request.getEventDate()
        );

        event.setTitle(
                request.getTitle().trim()
        );

        event.setEventType(
                request.getEventType()
        );

        event.setDescription(
                cleanDescription(
                        request.getDescription()
                )
        );

        event =
                calendarEventRepository.save(event);

        return new CalendarEventResponse(event);
    }

    // =====================================================
    // UPDATE EVENT
    // ADMIN ONLY
    // =====================================================

    @Transactional
    public CalendarEventResponse updateEvent(
            User user,
            Long eventId,
            CalendarEventRequest request
    ) {

        validateAdmin(user);

        validateRequest(request);

        if (eventId == null) {

            throw new RuntimeException(
                    "Calendar event ID is required."
            );
        }

        Company company =
                getCompany(user);

        String companyId =
                getCompanyId(company);

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompany_CompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Calendar event not found."
                                )
                        );

        event.setEventDate(
                request.getEventDate()
        );

        event.setTitle(
                request.getTitle().trim()
        );

        event.setEventType(
                request.getEventType()
        );

        event.setDescription(
                cleanDescription(
                        request.getDescription()
                )
        );

        event =
                calendarEventRepository.save(event);

        return new CalendarEventResponse(event);
    }

    // =====================================================
    // DELETE EVENT
    // ADMIN ONLY
    // =====================================================

    @Transactional
    public void deleteEvent(
            User user,
            Long eventId
    ) {

        validateAdmin(user);

        if (eventId == null) {

            throw new RuntimeException(
                    "Calendar event ID is required."
            );
        }

        Company company =
                getCompany(user);

        String companyId =
                getCompanyId(company);

        CalendarEvent event =
                calendarEventRepository
                        .findByIdAndCompany_CompanyId(
                                eventId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Calendar event not found."
                                )
                        );

        calendarEventRepository.delete(event);
    }

    // =====================================================
    // VALIDATE ADMIN
    // =====================================================

    private void validateAdmin(User user) {

        if (user == null) {

            throw new RuntimeException(
                    "User not found."
            );
        }

        if (user.getRole() == null ||
                !"ADMIN".equals(
                        user.getRole().name()
                )) {

            throw new RuntimeException(
                    "Only ADMIN can manage calendar events."
            );
        }

        if (!user.isActive()) {

            throw new RuntimeException(
                    "User account is inactive."
            );
        }
    }

    // =====================================================
    // GET COMPANY
    // =====================================================

    private Company getCompany(User user) {

        if (user == null) {

            throw new RuntimeException(
                    "User not found."
            );
        }

        if (user.getCompany() == null) {

            throw new RuntimeException(
                    "No company is assigned to this user."
            );
        }

        return user.getCompany();
    }

    // =====================================================
    // GET COMPANY ID
    // =====================================================

    private String getCompanyId(
            Company company
    ) {

        if (company == null) {

            throw new RuntimeException(
                    "Company not found."
            );
        }

        String companyId =
                company.getCompanyId();

        if (companyId == null ||
                companyId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Company ID is missing."
            );
        }

        return companyId;
    }

    // =====================================================
    // VALIDATE REQUEST
    // =====================================================

    private void validateRequest(
            CalendarEventRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Calendar event data is required."
            );
        }

        if (request.getEventDate() == null) {

            throw new RuntimeException(
                    "Event date is required."
            );
        }

        if (request.getTitle() == null ||
                request.getTitle().trim().isEmpty()) {

            throw new RuntimeException(
                    "Event title is required."
            );
        }

        if (request.getTitle().trim().length() > 150) {

            throw new RuntimeException(
                    "Event title cannot exceed 150 characters."
            );
        }

        if (request.getEventType() == null) {

            throw new RuntimeException(
                    "Event type is required."
            );
        }
    }

    // =====================================================
    // CLEAN DESCRIPTION
    // =====================================================

    private String cleanDescription(
            String description
    ) {

        if (description == null) {
            return null;
        }

        String cleaned =
                description.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}