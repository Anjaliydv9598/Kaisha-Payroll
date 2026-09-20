package com.kaisha.payroll.department.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.department.dto.PositionRequest;
import com.kaisha.payroll.department.dto.PositionResponse;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.entity.Position;
import com.kaisha.payroll.department.repository.DepartmentRepository;
import com.kaisha.payroll.department.repository.PositionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;

    public PositionService(
            PositionRepository positionRepository,
            DepartmentRepository departmentRepository,
            CurrentUserService currentUserService) {

        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getPositions(Long departmentId) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        Department department =
                departmentRepository
                        .findByDepartmentIdAndCompany_CompanyIdAndActiveTrue(
                                departmentId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found."
                                ));

        return positionRepository
                .findByDepartment_DepartmentIdAndActiveTrueOrderByPositionNameAsc(
                        departmentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PositionResponse addPosition(
            Long departmentId,
            PositionRequest request) {

        requireAdmin();

        if (request == null ||
                request.getPositionName() == null ||
                request.getPositionName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Position name is required."
            );
        }

        String companyId =
                currentUserService.getCurrentCompanyId();

        Department department =
                departmentRepository
                        .findByDepartmentIdAndCompany_CompanyIdAndActiveTrue(
                                departmentId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found."
                                ));

        String name =
                request.getPositionName().trim();

        if (positionRepository
                .existsByDepartment_DepartmentIdAndPositionNameIgnoreCaseAndActiveTrue(
                        departmentId,
                        name
                )) {

            throw new RuntimeException(
                    "Position already exists in this department."
            );
        }

        Position position = new Position();

        position.setDepartment(department);
        position.setPositionName(name);
        position.setActive(true);

        Position savedPosition =
                positionRepository.save(position);

        return toResponse(savedPosition);
    }

    @Transactional
    public PositionResponse updatePosition(
            Long positionId,
            PositionRequest request) {

        requireAdmin();

        if (request == null ||
                request.getPositionName() == null ||
                request.getPositionName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Position name is required."
            );
        }

        String companyId =
                currentUserService.getCurrentCompanyId();

        Position position =
                positionRepository
                        .findByPositionIdAndDepartment_Company_CompanyIdAndActiveTrue(
                                positionId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Position not found."
                                ));

        String name =
                request.getPositionName().trim();

        Long departmentId =
                position.getDepartment().getDepartmentId();

        if (positionRepository
                .existsByDepartment_DepartmentIdAndPositionNameIgnoreCaseAndActiveTrueAndPositionIdNot(
                        departmentId,
                        name,
                        positionId
                )) {

            throw new RuntimeException(
                    "Another position with this name already exists."
            );
        }

        position.setPositionName(name);

        Position savedPosition =
                positionRepository.save(position);

        return toResponse(savedPosition);
    }

    @Transactional
    public void deletePosition(Long positionId) {

        requireAdmin();

        String companyId =
                currentUserService.getCurrentCompanyId();

        Position position =
                positionRepository
                        .findByPositionIdAndDepartment_Company_CompanyIdAndActiveTrue(
                                positionId,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Position not found."
                                ));

        position.setActive(false);

        positionRepository.save(position);
    }

    private PositionResponse toResponse(Position position) {

        Department department =
                position.getDepartment();

        return new PositionResponse(
                position.getPositionId(),
                department.getDepartmentId(),
                department.getDepartmentName(),
                position.getPositionName(),
                position.isActive()
        );
    }

    private void requireAdmin() {

        User user =
                currentUserService.getCurrentUser();

        if (user.getRole() == null ||
                !"ADMIN".equals(user.getRole().name())) {

            throw new RuntimeException(
                    "Only ADMIN can perform this action."
            );
        }
    }
}