package com.kaisha.payroll.support.controller;

import com.kaisha.payroll.support.dto.SupportQueryRequest;
import com.kaisha.payroll.support.dto.SupportQueryStatusRequest;
import com.kaisha.payroll.support.entity.SupportQuery;
import com.kaisha.payroll.support.service.SupportQueryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin
public class SupportQueryController {

    private final SupportQueryService supportQueryService;

    public SupportQueryController(
            SupportQueryService supportQueryService
    ) {
        this.supportQueryService =
                supportQueryService;
    }


    /*
     * ------------------------------------------------
     * SUBMIT QUERY
     * ------------------------------------------------
     */
    @PostMapping("/query")
    public ResponseEntity<?> submitQuery(
            @RequestBody SupportQueryRequest request
    ) {

        try {

            SupportQuery saved =
                    supportQueryService.submitQuery(
                            request
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Thank you for contacting Kaisha Payroll Support. " +
                                    "Your query has been received. " +
                                    "We aim to resolve it within 36 hours.",

                            "queryId",
                            saved.getId(),

                            "status",
                            saved.getStatus().name()
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    /*
     * ------------------------------------------------
     * ADMIN - ALL QUERIES
     * ------------------------------------------------
     */
    @GetMapping("/queries")
    public ResponseEntity<?> getAllQueries() {

        try {

            List<SupportQuery> queries =
                    supportQueryService
                            .getAllQueries();

            return ResponseEntity.ok(
                    queries
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    /*
     * ------------------------------------------------
     * ADMIN - SINGLE QUERY
     * ------------------------------------------------
     */
    @GetMapping("/queries/{id}")
    public ResponseEntity<?> getQuery(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    supportQueryService
                            .getQueryById(id)
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    /*
     * ------------------------------------------------
     * ADMIN - OPEN COUNT
     * ------------------------------------------------
     */
    @GetMapping("/queries/open/count")
    public ResponseEntity<?> getOpenQueryCount() {

        try {

            long count =
                    supportQueryService
                            .getOpenQueryCount();

            return ResponseEntity.ok(
                    Map.of(
                            "count",
                            count
                    )
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }


    /*
     * ------------------------------------------------
     * ADMIN - UPDATE STATUS
     * ------------------------------------------------
     */
    @PutMapping("/queries/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody
            SupportQueryStatusRequest request
    ) {

        try {

            if (request == null ||
                    request.getStatus() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Status is required."
                                )
                        );
            }


            SupportQuery updated =
                    supportQueryService
                            .updateStatus(
                                    id,
                                    request.getStatus()
                            );

            return ResponseEntity.ok(
                    updated
            );

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    ex.getMessage()
                            )
                    );
        }
    }
}