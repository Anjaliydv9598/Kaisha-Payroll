
package com.kaisha.payroll.workjournal.controller;

import com.kaisha.payroll.workjournal.dto.WorkJournalRequest;
import com.kaisha.payroll.workjournal.dto.WorkJournalResponse;
import com.kaisha.payroll.workjournal.service.WorkJournalService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-journal")
@CrossOrigin
@PreAuthorize("hasRole('STAFF')")
public class WorkJournalController {

    private final WorkJournalService workJournalService;

    public WorkJournalController(
            WorkJournalService workJournalService
    ) {
        this.workJournalService = workJournalService;
    }

    // ---------------------------------------------------------
    // CREATE DAILY WORK ENTRY
    // ---------------------------------------------------------

    @PostMapping("/entries")
    public ResponseEntity<WorkJournalResponse> createEntry(
            @Valid @RequestBody WorkJournalRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(workJournalService.createEntry(request));
    }

    // ---------------------------------------------------------
    // GET ALL MY WORK ENTRIES
    // ---------------------------------------------------------

    @GetMapping("/entries")
    public ResponseEntity<List<WorkJournalResponse>> getMyEntries() {

        return ResponseEntity.ok(
                workJournalService.getMyEntries()
        );
    }

    // ---------------------------------------------------------
    // GET ENTRY BY ID
    // ---------------------------------------------------------

    @GetMapping("/entries/{id}")
    public ResponseEntity<WorkJournalResponse> getEntryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                workJournalService.getEntryById(id)
        );
    }

    // ---------------------------------------------------------
    // UPDATE MY WORK ENTRY
    // ---------------------------------------------------------

    @PutMapping("/entries/{id}")
    public ResponseEntity<WorkJournalResponse> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody WorkJournalRequest request
    ) {
        return ResponseEntity.ok(
                workJournalService.updateEntry(id, request)
        );
    }

    // ---------------------------------------------------------
    // DELETE MY WORK ENTRY
    // ---------------------------------------------------------

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Map<String, String>> deleteEntry(
            @PathVariable Long id
    ) {

        workJournalService.deleteEntry(id);

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                "Work journal entry deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // GET MY HIGHLIGHTS
    // ---------------------------------------------------------

    @GetMapping("/highlights")
    public ResponseEntity<List<WorkJournalResponse>> getMyHighlights() {

        return ResponseEntity.ok(
                workJournalService.getMyHighlights()
        );
    }

    // ---------------------------------------------------------
    // GET MY ENTRIES BY DATE RANGE
    // ---------------------------------------------------------

    @GetMapping("/entries/range")
    public ResponseEntity<List<WorkJournalResponse>> getEntriesByDateRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(
                workJournalService.getEntriesByDateRange(from, to)
        );
    }
}
