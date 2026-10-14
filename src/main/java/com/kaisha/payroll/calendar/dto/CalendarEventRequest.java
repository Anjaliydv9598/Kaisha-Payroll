package com.kaisha.payroll.calendar.dto;

import com.kaisha.payroll.calendar.entity.CalendarEventType;

import java.time.LocalDate;

public class CalendarEventRequest {

    private LocalDate eventDate;

    private String title;

    private CalendarEventType eventType;

    private String description;

    // =====================================================
    // GETTERS
    // =====================================================

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

    // =====================================================
    // SETTERS
    // =====================================================

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
}