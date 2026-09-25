package com.kaisha.payroll.calendar.dto;

import com.kaisha.payroll.calendar.entity.CalendarEvent;
import com.kaisha.payroll.calendar.entity.CalendarEventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CalendarEventResponse {

    private Long id;

    private LocalDate eventDate;

    private String title;

    private CalendarEventType eventType;

    private String description;

    private String companyId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CalendarEventResponse() {
    }

    public CalendarEventResponse(CalendarEvent event) {

        this.id = event.getId();

        this.eventDate = event.getEventDate();

        this.title = event.getTitle();

        this.eventType = event.getEventType();

        this.description = event.getDescription();

        if (event.getCompany() != null) {
            this.companyId =
                    event.getCompany().getCompanyId();
        }

        this.createdAt = event.getCreatedAt();

        this.updatedAt = event.getUpdatedAt();
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getTitle() {
        return title;
    }

    public CalendarEventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getCompanyId() {
        return companyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEventType(CalendarEventType eventType) {
        this.eventType = eventType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}