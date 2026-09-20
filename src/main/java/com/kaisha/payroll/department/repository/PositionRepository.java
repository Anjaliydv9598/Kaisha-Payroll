package com.kaisha.payroll.department.repository;

import com.kaisha.payroll.department.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository
        extends JpaRepository<Position, Long> {

    List<Position>
    findByDepartment_DepartmentIdAndActiveTrueOrderByPositionNameAsc(
            Long departmentId
    );

    List<Position>
    findByDepartment_DepartmentIdAndActiveTrue(
            Long departmentId
    );

    Optional<Position>
    findByPositionIdAndDepartment_Company_CompanyIdAndActiveTrue(
            Long positionId,
            String companyId
    );

    boolean
    existsByDepartment_DepartmentIdAndPositionNameIgnoreCaseAndActiveTrue(
            Long departmentId,
            String positionName
    );

    boolean
    existsByDepartment_DepartmentIdAndPositionNameIgnoreCaseAndActiveTrueAndPositionIdNot(
            Long departmentId,
            String positionName,
            Long positionId
    );
}