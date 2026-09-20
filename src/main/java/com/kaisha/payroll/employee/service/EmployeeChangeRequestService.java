package com.kaisha.payroll.employee.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeChangeRequest;
import com.kaisha.payroll.employee.entity.EmployeeChangeRequestStatus;
import com.kaisha.payroll.employee.entity.EmployeeChangeRequestType;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.repository.EmployeeChangeRequestRepository;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeChangeRequestService {

    private final EmployeeChangeRequestRepository requestRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFieldRepository fieldRepository;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    public EmployeeChangeRequestService(
            EmployeeChangeRequestRepository requestRepository,
            EmployeeRepository employeeRepository,
            EmployeeFieldRepository fieldRepository,
            EmployeeService employeeService,
            CurrentUserService currentUserService
    ) {
        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
        this.fieldRepository = fieldRepository;
        this.employeeService = employeeService;
        this.currentUserService = currentUserService;
    }

    // ============================================================
    // STAFF CREATE REQUEST
    // ============================================================

    @Transactional
    public Map<String, Object> createRequest(
            String employeeId,
            Map<String, String> dto
    ) {

        User requestedBy =
                currentUserService.getCurrentUser();

        if (
                requestedBy == null
                        ||
                        requestedBy.getRole() == null
                        ||
                        !"STAFF".equals(
                                requestedBy
                                        .getRole()
                                        .name()
                        )
        ) {

            throw new RuntimeException(
                    "Only STAFF can create employee change requests"
            );
        }

        String companyId =
                currentUserService.getCurrentCompanyId();

        Employee employee =
                employeeRepository
                        .findByEmployeeIdAndCompany_CompanyId(
                                employeeId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );

        String requestTypeValue =
                dto.get("requestType");

        if (
                requestTypeValue == null
                        ||
                        requestTypeValue.isBlank()
        ) {

            throw new RuntimeException(
                    "Request type is required"
            );
        }

        EmployeeChangeRequestType requestType;

        try {

            requestType =
                    EmployeeChangeRequestType.valueOf(
                            requestTypeValue
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException ex) {

            throw new RuntimeException(
                    "Invalid employee request type"
            );
        }

        EmployeeChangeRequest request =
                new EmployeeChangeRequest();

        request.setCompany(
                employee.getCompany()
        );

        request.setRequestedBy(
                requestedBy
        );

        request.setEmployee(
                employee
        );

        request.setRequestType(
                requestType
        );

        request.setReason(
                dto.get("reason")
        );

        // ========================================================
        // ADD FIELD
        // ========================================================

        if (
                requestType ==
                        EmployeeChangeRequestType.ADD_FIELD
        ) {

            String fieldName =
                    dto.get("fieldName");

            String fieldValue =
                    dto.get("fieldValue");

            validateFieldName(fieldName);

            if (
                    fieldRepository
                            .existsByEmployee_IdAndFieldNameIgnoreCase(
                                    employee.getId(),
                                    fieldName.trim()
                            )
            ) {

                throw new RuntimeException(
                        "This field already exists"
                );
            }

            request.setFieldName(
                    fieldName.trim()
            );

            request.setFieldValue(
                    fieldValue
            );
        }

        // ========================================================
        // EDIT FIELD
        // ========================================================

        else if (
                requestType ==
                        EmployeeChangeRequestType.EDIT_FIELD
        ) {

            Long fieldId =
                    parseFieldId(
                            dto.get("fieldId")
                    );

            EmployeeField field =
                    fieldRepository
                            .findByFieldIdAndEmployee_Id(
                                    fieldId,
                                    employee.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Employee field not found"
                                    )
                            );

            String fieldName =
                    dto.get("fieldName");

            validateFieldName(fieldName);

            if (
                    fieldRepository
                            .existsByEmployee_IdAndFieldNameIgnoreCaseAndFieldIdNot(
                                    employee.getId(),
                                    fieldName.trim(),
                                    fieldId
                            )
            ) {

                throw new RuntimeException(
                        "This field already exists"
                );
            }

            request.setEmployeeField(
                    field
            );

            request.setOldFieldName(
                    field.getFieldName()
            );

            request.setFieldName(
                    fieldName.trim()
            );

            request.setFieldValue(
                    dto.get("fieldValue")
            );
        }

        // ========================================================
        // DELETE FIELD
        // ========================================================

        else if (
                requestType ==
                        EmployeeChangeRequestType.DELETE_FIELD
        ) {

            Long fieldId =
                    parseFieldId(
                            dto.get("fieldId")
                    );

            EmployeeField field =
                    fieldRepository
                            .findByFieldIdAndEmployee_Id(
                                    fieldId,
                                    employee.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Employee field not found"
                                    )
                            );

            request.setEmployeeField(
                    field
            );

            request.setOldFieldName(
                    field.getFieldName()
            );

            request.setFieldName(
                    field.getFieldName()
            );

            request.setFieldValue(
                    field.getFieldValue()
            );
        }

        // ========================================================
        // EDIT EMPLOYEE ID PREFIX
        //
        // E001 -> T001
        //
        // oldFieldName = old prefix
        // fieldName    = new prefix
        // ========================================================

        else if (
                requestType ==
                        EmployeeChangeRequestType
                                .EDIT_EMPLOYEE_ID_PREFIX
        ) {

            String newPrefix =
                    dto.get("fieldName");

            validatePrefix(newPrefix);

            String currentEmployeeId =
                    employee.getEmployeeId();

            String oldPrefix =
                    currentEmployeeId
                            .replaceAll(
                                    "\\d+$",
                                    ""
                            );

            request.setOldFieldName(
                    oldPrefix
            );

            request.setFieldName(
                    newPrefix
                            .trim()
                            .toUpperCase()
            );

            request.setFieldValue(null);
        }

        request.setStatus(
                EmployeeChangeRequestStatus.PENDING
        );

        EmployeeChangeRequest saved =
                requestRepository.save(request);

        return toResponse(saved);
    }

    // ============================================================
    // ADMIN PENDING REQUESTS
    // ============================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingRequests() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return requestRepository
                .findByCompany_CompanyIdAndStatusOrderByCreatedAtDesc(
                        companyId,
                        EmployeeChangeRequestStatus.PENDING
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // ADMIN APPROVE
    // ============================================================

    @Transactional
    public Map<String, Object> approveRequest(
            Long requestId
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        User reviewer =
                currentUserService.getCurrentUser();

        EmployeeChangeRequest request =
                requestRepository
                        .findByRequestIdAndCompany_CompanyId(
                                requestId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        if (
                request.getStatus()
                        != EmployeeChangeRequestStatus.PENDING
        ) {

            throw new RuntimeException(
                    "Request has already been reviewed"
            );
        }

        Employee employee =
                request.getEmployee();

        switch (request.getRequestType()) {

            case ADD_FIELD:

                employeeService.addFieldForApproval(
                        employee,
                        request.getFieldName(),
                        request.getFieldValue()
                );

                break;

            case EDIT_FIELD:

                if (
                        request.getEmployeeField()
                                == null
                ) {

                    throw new RuntimeException(
                            "Employee field not found"
                    );
                }

                employeeService.updateFieldForApproval(
                        employee,
                        request.getEmployeeField()
                                .getFieldId(),
                        request.getFieldName(),
                        request.getFieldValue()
                );

                break;

            case DELETE_FIELD:

                if (
                        request.getEmployeeField()
                                == null
                ) {

                    throw new RuntimeException(
                            "Employee field not found"
                    );
                }

                employeeService.deleteFieldForApproval(
                        employee,
                        request.getEmployeeField()
                                .getFieldId()
                );

                break;

            case EDIT_EMPLOYEE_ID_PREFIX:

                employeeService
                        .updateEmployeePrefixForApproval(
                                employee,
                                request.getFieldName()
                        );

                break;
        }

        request.setStatus(
                EmployeeChangeRequestStatus.APPROVED
        );

        request.setReviewedBy(
                reviewer
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        requestRepository.save(request);

        return toResponse(request);
    }

    // ============================================================
    // ADMIN REJECT
    // ============================================================

    @Transactional
    public Map<String, Object> rejectRequest(
            Long requestId
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        User reviewer =
                currentUserService.getCurrentUser();

        EmployeeChangeRequest request =
                requestRepository
                        .findByRequestIdAndCompany_CompanyId(
                                requestId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Request not found"
                                )
                        );

        if (
                request.getStatus()
                        != EmployeeChangeRequestStatus.PENDING
        ) {

            throw new RuntimeException(
                    "Request has already been reviewed"
            );
        }

        request.setStatus(
                EmployeeChangeRequestStatus.REJECTED
        );

        request.setReviewedBy(
                reviewer
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        requestRepository.save(request);

        return toResponse(request);
    }

    // ============================================================
    // RESPONSE
    // ============================================================

    private Map<String, Object> toResponse(
            EmployeeChangeRequest request
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "requestId",
                request.getRequestId()
        );

        response.put(
                "employeeId",
                request.getEmployee()
                        .getEmployeeId()
        );

        response.put(
                "fieldId",
                request.getEmployeeField() != null
                        ? request.getEmployeeField()
                          .getFieldId()
                        : null
        );

        response.put(
                "requestType",
                request.getRequestType()
        );

        response.put(
                "fieldName",
                request.getFieldName()
        );

        response.put(
                "oldFieldName",
                request.getOldFieldName()
        );

        response.put(
                "fieldValue",
                request.getFieldValue()
        );

        response.put(
                "reason",
                request.getReason()
        );

        response.put(
                "status",
                request.getStatus()
        );

        response.put(
                "requestedBy",
                request.getRequestedBy()
                        != null
                        ? request.getRequestedBy()
                          .getUsername()
                        : null
        );

        response.put(
                "createdAt",
                request.getCreatedAt()
        );

        return response;
    }

    private Long parseFieldId(
            String fieldId
    ) {

        if (
                fieldId == null
                        ||
                        fieldId.isBlank()
        ) {

            throw new RuntimeException(
                    "Field ID is required"
            );
        }

        try {

            return Long.parseLong(
                    fieldId
            );

        } catch (NumberFormatException ex) {

            throw new RuntimeException(
                    "Invalid field ID"
            );
        }
    }

    private void validateFieldName(
            String fieldName
    ) {

        if (
                fieldName == null
                        ||
                        fieldName.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Field name is required"
            );
        }
    }

    private void validatePrefix(
            String prefix
    ) {

        if (
                prefix == null
                        ||
                        prefix.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Prefix is required"
            );
        }

        if (
                !prefix
                        .trim()
                        .matches("[A-Za-z]+")
        ) {

            throw new RuntimeException(
                    "Prefix can contain letters only"
            );
        }
    }
}