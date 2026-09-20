package com.kaisha.payroll.department.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.department.dto.DepartmentRequest;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.repository.DepartmentRepository;
import com.kaisha.payroll.department.repository.PositionRepository;
import com.kaisha.payroll.auth.service.CurrentUserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final CurrentUserService currentUserService;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            CurrentUserService currentUserService) {

        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartments() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return departmentRepository
                .findByCompany_CompanyIdAndActiveTrueOrderByDepartmentNameAsc(
                        companyId
                );
    }

    @Transactional
    public Department addDepartment(
            DepartmentRequest request) {

        requireAdmin();

        if (request == null ||
                request.getDepartmentName() == null ||
                request.getDepartmentName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Department name is required."
            );
        }

        String name = request
                .getDepartmentName()
                .trim();

        String companyId =
                currentUserService.getCurrentCompanyId();

        if (departmentRepository
                .existsByCompany_CompanyIdAndDepartmentNameIgnoreCaseAndActiveTrue(
                        companyId,
                        name)) {

            throw new RuntimeException(
                    "Department already exists."
            );
        }

        User user = currentUserService.getCurrentUser();

        Department department = new Department();

        department.setCompany(user.getCompany());
        department.setDepartmentName(name);
        department.setActive(true);

        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(
            Long departmentId,
            DepartmentRequest request) {

        requireAdmin();

        if (request == null ||
                request.getDepartmentName() == null ||
                request.getDepartmentName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Department name is required."
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
                request.getDepartmentName().trim();

        if (departmentRepository
                .existsByCompany_CompanyIdAndDepartmentNameIgnoreCaseAndActiveTrueAndDepartmentIdNot(
                        companyId,
                        name,
                        departmentId
                )) {

            throw new RuntimeException(
                    "Another department with this name already exists."
            );
        }

        department.setDepartmentName(name);

        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {

        requireAdmin();

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

        /*
         * Soft delete.
         * We do NOT physically remove the record.
         */
        department.setActive(false);
        departmentRepository.save(department);

        /*
         * Also deactivate all positions
         * belonging to this department.
         */
        List<com.kaisha.payroll.department.entity.Position>
                positions =
                positionRepository
                        .findByDepartment_DepartmentIdAndActiveTrue(
                                departmentId
                        );

        for (var position : positions) {
            position.setActive(false);
        }

        positionRepository.saveAll(positions);
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