package com.kaisha.payroll.employee.repository;

import com.kaisha.payroll.employee.entity.EmployeeField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeFieldRepository
        extends JpaRepository<EmployeeField, Long> {

    List<EmployeeField>
    findByEmployee_IdOrderByFieldIdAsc(
            Long employeeId
    );

    Optional<EmployeeField>
    findByFieldIdAndEmployee_Id(
            Long fieldId,
            Long employeeId
    );

    boolean
    existsByEmployee_IdAndFieldNameIgnoreCase(
            Long employeeId,
            String fieldName
    );

    boolean
    existsByEmployee_IdAndFieldNameIgnoreCaseAndFieldIdNot(
            Long employeeId,
            String fieldName,
            Long fieldId
    );
}