package com.kaisha.payroll.department.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.CurrentUserService;

import com.kaisha.payroll.department.dto.*;
import com.kaisha.payroll.department.entity.*;
import com.kaisha.payroll.department.entity.DepartmentChangeRequestStatus;
import com.kaisha.payroll.department.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentChangeRequestService {

    private final DepartmentChangeRequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final CurrentUserService currentUserService;

    public DepartmentChangeRequestService(
            DepartmentChangeRequestRepository requestRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            DepartmentService departmentService,
            PositionService positionService,
            CurrentUserService currentUserService) {

        this.requestRepository = requestRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.departmentService = departmentService;
        this.positionService = positionService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public DepartmentChangeRequestResponse createRequest(
            DepartmentChangeRequestDto dto) {

        User staff =
                currentUserService.getCurrentUser();

        requireStaff(staff);

        if (dto == null ||
                dto.getRequestType() == null) {

            throw new RuntimeException(
                    "Request type is required."
            );
        }

        DepartmentChangeRequest request =
                new DepartmentChangeRequest();

        request.setCompany(staff.getCompany());
        request.setRequestedBy(staff);
        request.setRequestType(dto.getRequestType());
        request.setReason(
                clean(dto.getReason())
        );
        request.setStatus(
                DepartmentChangeRequestStatus.PENDING
        );

        switch (dto.getRequestType()) {

            case ADD_DEPARTMENT:

                request.setRequestedName(
                        requiredName(dto.getRequestedName())
                );

                break;

            case EDIT_DEPARTMENT:

                Department department =
                        findDepartment(
                                dto.getDepartmentId()
                        );

                request.setDepartment(department);

                request.setOldName(
                        department.getDepartmentName()
                );

                request.setRequestedName(
                        requiredName(dto.getRequestedName())
                );

                break;

            case ADD_POSITION:

                Department positionDepartment =
                        findDepartment(
                                dto.getDepartmentId()
                        );

                request.setDepartment(
                        positionDepartment
                );

                request.setRequestedName(
                        requiredName(dto.getRequestedName())
                );

                break;

            case EDIT_POSITION:

                Position editPosition =
                        findPosition(
                                dto.getPositionId()
                        );

                request.setPosition(
                        editPosition
                );

                request.setDepartment(
                        editPosition.getDepartment()
                );

                request.setOldName(
                        editPosition.getPositionName()
                );

                request.setRequestedName(
                        requiredName(dto.getRequestedName())
                );

                break;

            case DELETE_POSITION:

                Position deletePosition =
                        findPosition(
                                dto.getPositionId()
                        );

                request.setPosition(
                        deletePosition
                );

                request.setDepartment(
                        deletePosition.getDepartment()
                );

                request.setOldName(
                        deletePosition.getPositionName()
                );

                break;

            default:

                throw new RuntimeException(
                        "Unsupported request type."
                );
        }

        return toResponse(
                requestRepository.save(request)
        );
    }

    @Transactional(readOnly = true)
    public List<DepartmentChangeRequestResponse>
    getPendingRequests() {

        User admin =
                currentUserService.getCurrentUser();

        requireAdmin(admin);

        String companyId =
                admin.getCompany().getCompanyId();

        return requestRepository
                .findByCompany_CompanyIdAndStatusOrderByCreatedAtDesc(
                        companyId,
                        DepartmentChangeRequestStatus.PENDING
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentChangeRequestResponse approveRequest(
            Long requestId) {

        User admin =
                currentUserService.getCurrentUser();

        requireAdmin(admin);

        DepartmentChangeRequest request =
                findRequestForCompany(requestId, admin);

        if (request.getStatus() !=
                DepartmentChangeRequestStatus.PENDING) {

            throw new RuntimeException(
                    "This request has already been reviewed."
            );
        }

        applyRequest(request);

        request.setStatus(
                DepartmentChangeRequestStatus.APPROVED
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        request.setReviewedBy(admin);

        return toResponse(
                requestRepository.save(request)
        );
    }

    @Transactional
    public DepartmentChangeRequestResponse rejectRequest(
            Long requestId) {

        User admin =
                currentUserService.getCurrentUser();

        requireAdmin(admin);

        DepartmentChangeRequest request =
                findRequestForCompany(requestId, admin);

        if (request.getStatus() !=
                DepartmentChangeRequestStatus.PENDING) {

            throw new RuntimeException(
                    "This request has already been reviewed."
            );
        }

        request.setStatus(
                DepartmentChangeRequestStatus.REJECTED
        );

        request.setReviewedAt(
                LocalDateTime.now()
        );

        request.setReviewedBy(admin);

        return toResponse(
                requestRepository.save(request)
        );
    }

    private void applyRequest(
            DepartmentChangeRequest request) {

        switch (request.getRequestType()) {

            case ADD_DEPARTMENT:

                DepartmentRequest departmentRequest =
                        new DepartmentRequest();

                departmentRequest.setDepartmentName(
                        request.getRequestedName()
                );

                departmentService.addDepartment(
                        departmentRequest
                );

                break;

            case EDIT_DEPARTMENT:

                DepartmentRequest editDepartmentRequest =
                        new DepartmentRequest();

                editDepartmentRequest.setDepartmentName(
                        request.getRequestedName()
                );

                departmentService.updateDepartment(
                        request.getDepartment().getDepartmentId(),
                        editDepartmentRequest
                );

                break;

            case ADD_POSITION:

                PositionRequest positionRequest =
                        new PositionRequest();

                positionRequest.setPositionName(
                        request.getRequestedName()
                );

                positionService.addPosition(
                        request.getDepartment().getDepartmentId(),
                        positionRequest
                );

                break;

            case EDIT_POSITION:

                PositionRequest editPositionRequest =
                        new PositionRequest();

                editPositionRequest.setPositionName(
                        request.getRequestedName()
                );

                positionService.updatePosition(
                        request.getPosition().getPositionId(),
                        editPositionRequest
                );

                break;

            case DELETE_POSITION:

                positionService.deletePosition(
                        request.getPosition().getPositionId()
                );

                break;

            default:

                throw new RuntimeException(
                        "Unsupported request type."
                );
        }
    }

    private Department findDepartment(
            Long departmentId) {

        if (departmentId == null) {
            throw new RuntimeException(
                    "Department ID is required."
            );
        }

        String companyId =
                currentUserService.getCurrentCompanyId();

        return departmentRepository
                .findByDepartmentIdAndCompany_CompanyIdAndActiveTrue(
                        departmentId,
                        companyId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found."
                        ));
    }

    private Position findPosition(
            Long positionId) {

        if (positionId == null) {
            throw new RuntimeException(
                    "Position ID is required."
            );
        }

        String companyId =
                currentUserService.getCurrentCompanyId();

        return positionRepository
                .findByPositionIdAndDepartment_Company_CompanyIdAndActiveTrue(
                        positionId,
                        companyId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Position not found."
                        ));
    }

    private DepartmentChangeRequest findRequestForCompany(
            Long requestId,
            User admin) {

        return requestRepository
                .findByRequestIdAndCompany_CompanyId(
                        requestId,
                        admin.getCompany().getCompanyId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Request not found."
                        ));
    }

    private String requiredName(String name) {

        if (name == null || name.trim().isEmpty()) {

            throw new RuntimeException(
                    "Requested name is required."
            );
        }

        return name.trim();
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String result = value.trim();

        return result.isEmpty() ? null : result;
    }

    private void requireStaff(User user) {

        if (user == null ||
                user.getRole() == null ||
                !"STAFF".equals(user.getRole().name())) {

            throw new RuntimeException(
                    "Only STAFF can create change requests."
            );
        }
    }

    private void requireAdmin(User user) {

        if (user == null ||
                user.getRole() == null ||
                !"ADMIN".equals(user.getRole().name())) {

            throw new RuntimeException(
                    "Only ADMIN can review requests."
            );
        }
    }

    private DepartmentChangeRequestResponse
    toResponse(DepartmentChangeRequest request) {

        DepartmentChangeRequestResponse response =
                new DepartmentChangeRequestResponse();

        response.setRequestId(
                request.getRequestId()
        );

        response.setRequestType(
                request.getRequestType()
        );

        if (request.getDepartment() != null) {

            response.setDepartmentId(
                    request.getDepartment().getDepartmentId()
            );

            response.setDepartmentName(
                    request.getDepartment().getDepartmentName()
            );
        }

        if (request.getPosition() != null) {

            response.setPositionId(
                    request.getPosition().getPositionId()
            );

            response.setPositionName(
                    request.getPosition().getPositionName()
            );
        }

        response.setRequestedName(
                request.getRequestedName()
        );

        response.setOldName(
                request.getOldName()
        );

        response.setReason(
                request.getReason()
        );

        response.setStatus(
                request.getStatus()
        );

        if (request.getRequestedBy() != null) {

            response.setRequestedBy(
                    request.getRequestedBy().getEmail()
            );
        }

        response.setCreatedAt(
                request.getCreatedAt()
        );

        return response;
    }
}