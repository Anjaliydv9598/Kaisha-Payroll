package com.kaisha.payroll.employee.numberseries.controller;

import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesRequest;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesResponse;
import com.kaisha.payroll.employee.numberseries.service.EmployeeNumberSeriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee-number-series")
@CrossOrigin
public class EmployeeNumberSeriesController {

    private final EmployeeNumberSeriesService seriesService;

    public EmployeeNumberSeriesController(
            EmployeeNumberSeriesService seriesService
    ) {

        this.seriesService =
                seriesService;
    }

    // ============================================================
    // GET ALL NUMBER SERIES
    // ADMIN ONLY
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NumberSeriesResponse>>
    getAllSeries() {

        return ResponseEntity.ok(
                seriesService.getAllSeries()
        );
    }

    // ============================================================
    // GET ONE NUMBER SERIES
    // ADMIN ONLY
    // ============================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NumberSeriesResponse>
    getSeries(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                seriesService.getSeries(id)
        );
    }

    // ============================================================
    // CREATE NUMBER SERIES
    // ADMIN ONLY
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NumberSeriesResponse>
    createSeries(
            @RequestBody NumberSeriesRequest request
    ) {

        NumberSeriesResponse response =
                seriesService.createSeries(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ============================================================
    // UPDATE NUMBER SERIES
    // ADMIN ONLY
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NumberSeriesResponse>
    updateSeries(
            @PathVariable Long id,
            @RequestBody NumberSeriesRequest request
    ) {

        return ResponseEntity.ok(
                seriesService.updateSeries(
                        id,
                        request
                )
        );
    }

    // ============================================================
    // DELETE NUMBER SERIES
    // ADMIN ONLY
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSeries(
            @PathVariable Long id
    ) {

        seriesService.deleteSeries(id);

        Map<String, String> response =
                new HashMap<>();

        response.put(
                "message",
                "Number series deleted successfully"
        );

        return ResponseEntity.ok(response);
    }
}