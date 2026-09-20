package com.kaisha.payroll.company.controller;

import com.kaisha.payroll.company.dto.CompanyFieldRequest;
import com.kaisha.payroll.company.entity.CompanyField;
import com.kaisha.payroll.company.entity.CompanyFieldValue;
import com.kaisha.payroll.company.repository.CompanyFieldValueRepository;
import com.kaisha.payroll.company.service.CompanyFieldService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company/fields")
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyFieldController {

    private final CompanyFieldService companyFieldService;
    private final CompanyFieldValueRepository companyFieldValueRepository;

    public CompanyFieldController(
            CompanyFieldService companyFieldService,
            CompanyFieldValueRepository companyFieldValueRepository
    ) {
        this.companyFieldService = companyFieldService;
        this.companyFieldValueRepository = companyFieldValueRepository;
    }

    // =========================================================
    // GET ALL ACTIVE DYNAMIC COMPANY FIELDS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFields() {

        List<CompanyField> fields =
                companyFieldService.getFieldsForCurrentCompany();

        return ResponseEntity.ok(
                convertToResponse(fields)
        );
    }

    // =========================================================
    // ADD DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addField(
            @RequestBody CompanyFieldRequest request
    ) {

        try {

            CompanyField field =
                    companyFieldService.addField(request);

            return ResponseEntity.ok(
                    convertSingleField(field)
            );

        } catch (RuntimeException e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    // =========================================================
    // UPDATE DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateField(
            @PathVariable Long fieldId,
            @RequestBody CompanyFieldRequest request
    ) {

        try {

            CompanyField field =
                    companyFieldService.updateField(
                            fieldId,
                            request
                    );

            return ResponseEntity.ok(
                    convertSingleField(field)
            );

        } catch (RuntimeException e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    // =========================================================
    // DELETE ONE DYNAMIC COMPANY FIELD
    // ADMIN ONLY
    // =========================================================

    @DeleteMapping("/{fieldId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteField(
            @PathVariable Long fieldId
    ) {

        try {

            companyFieldService.deleteField(fieldId);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Company field deleted successfully."
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    // =========================================================
    // CONVERT LIST TO FRONTEND RESPONSE
    // =========================================================

    private List<Map<String, Object>> convertToResponse(
            List<CompanyField> fields
    ) {

        List<Map<String, Object>> response =
                new ArrayList<>();

        for (CompanyField field : fields) {

            response.add(
                    convertSingleField(field)
            );
        }

        return response;
    }

    // =========================================================
    // CONVERT ONE FIELD
    // =========================================================

    private Map<String, Object> convertSingleField(
            CompanyField field
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "fieldId",
                field.getFieldId()
        );

        response.put(
                "fieldName",
                field.getFieldName()
        );

        String fieldValue = "";

        /*
         * One CompanyField has one CompanyFieldValue.
         */

        CompanyFieldValue value =
                companyFieldValueRepository
                        .findFirstByField_FieldId(
                                field.getFieldId()
                        )
                        .orElse(null);

        if (value != null) {

            fieldValue =
                    value.getFieldValue();
        }

        response.put(
                "fieldValue",
                fieldValue
        );

        return response;
    }
}