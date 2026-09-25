package com.kaisha.payroll.calendar.repository;

import com.kaisha.payroll.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository
        extends JpaRepository<CalendarEvent, Long> {

    /*
     * Get all calendar events for one company.
     *
     * Your Company entity uses companyId,
     * not getId().
     */
    List<CalendarEvent>
    findByCompany_CompanyIdOrderByEventDateAsc(
            String companyId
    );

    /*
     * Get calendar events for a particular year.
     */
    List<CalendarEvent>
    findByCompany_CompanyIdAndEventDateBetweenOrderByEventDateAsc(
            String companyId,
            LocalDate startDate,
            LocalDate endDate
    );

    /*
     * Find one event only if it belongs
     * to the current company.
     */
    Optional<CalendarEvent>
    findByIdAndCompany_CompanyId(
            Long id,
            String companyId
    );
}