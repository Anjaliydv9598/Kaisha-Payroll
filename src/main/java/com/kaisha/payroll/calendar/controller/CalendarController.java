package com.kaisha.payroll.calendar.controller;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.calendar.dto.CalendarEventRequest;
import com.kaisha.payroll.calendar.dto.CalendarEventResponse;
import com.kaisha.payroll.calendar.service.CalendarService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin
public class CalendarController {

    private final CalendarService calendarService;

    private final UserRepository userRepository;

    public CalendarController(
            CalendarService calendarService,
            UserRepository userRepository
    ) {

        this.calendarService =
                calendarService;

        this.userRepository =
                userRepository;
    }

    // =====================================================
    // GET ALL CALENDAR EVENTS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEvents(
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            List<CalendarEventResponse> events =
                    calendarService.getAllEvents(user);

            return ResponseEntity.ok(events);

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // GET EVENTS BY YEAR
    // =====================================================

    @GetMapping("/year/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEventsByYear(
            @PathVariable int year,
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            List<CalendarEventResponse> events =
                    calendarService.getEventsByYear(
                            user,
                            year
                    );

            return ResponseEntity.ok(events);

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // GET ONE EVENT
    // =====================================================

    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEventById(
            @PathVariable Long eventId,
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            CalendarEventResponse event =
                    calendarService.getEventById(
                            user,
                            eventId
                    );

            return ResponseEntity.ok(event);

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // CREATE EVENT
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(
            @RequestBody CalendarEventRequest request,
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            CalendarEventResponse response =
                    calendarService.createEvent(
                            user,
                            request
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // UPDATE EVENT
    // =====================================================

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long eventId,
            @RequestBody CalendarEventRequest request,
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            CalendarEventResponse response =
                    calendarService.updateEvent(
                            user,
                            eventId,
                            request
                    );

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // DELETE EVENT
    // =====================================================

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {

        try {

            User user =
                    getLoggedInUser(authentication);

            calendarService.deleteEvent(
                    user,
                    eventId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Calendar event deleted successfully."
                    )
            );

        } catch (RuntimeException ex) {

            return errorResponse(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    // =====================================================
    // GET LOGGED-IN USER
    // =====================================================

    private User getLoggedInUser(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "User authentication is required."
            );
        }

        String username =
                authentication.getName();

        /*
         * Your JWT subject is the username/email.
         * UserService already uses email for login.
         */
        return userRepository
                .findByEmail(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found."
                        )
                );
    }

    // =====================================================
    // ERROR RESPONSE
    // =====================================================

    private ResponseEntity<Map<String, String>>
    errorResponse(
            HttpStatus status,
            String message
    ) {

        return ResponseEntity
                .status(status)
                .body(
                        Map.of(
                                "message",
                                message == null
                                        ? "Request failed."
                                        : message
                        )
                );
    }
}