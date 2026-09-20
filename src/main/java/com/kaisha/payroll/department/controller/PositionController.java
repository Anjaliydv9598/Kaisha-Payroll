package com.kaisha.payroll.department.controller;

import com.kaisha.payroll.department.dto.PositionRequest;
import com.kaisha.payroll.department.service.PositionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "http://localhost:5173")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    // =========================================================
    // GET POSITIONS
    // =========================================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getPositions(
            @PathVariable Long departmentId) {

        try {
            return ResponseEntity.ok(
                    positionService.getPositions(departmentId)
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // ADD POSITION
    // ADMIN ONLY
    // =========================================================

    @PostMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addPosition(
            @PathVariable Long departmentId,
            @RequestBody PositionRequest request) {

        try {
            return ResponseEntity.ok(
                    positionService.addPosition(
                            departmentId,
                            request
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // EDIT POSITION
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{positionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePosition(
            @PathVariable Long positionId,
            @RequestBody PositionRequest request) {

        try {
            return ResponseEntity.ok(
                    positionService.updatePosition(
                            positionId,
                            request
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // DELETE POSITION
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{positionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePosition(
            @PathVariable Long positionId) {

        try {

            positionService.deletePosition(positionId);

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "message",
                    "Position deleted successfully."
            );

            response.put(
                    "positionId",
                    positionId
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            Map<String, Object> error = new HashMap<>();

            error.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }
}