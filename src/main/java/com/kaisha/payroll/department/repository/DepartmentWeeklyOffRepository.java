package com.kaisha.payroll.department.repository;

import com.kaisha.payroll.department.entity.DepartmentWeeklyOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentWeeklyOffRepository
        extends JpaRepository<DepartmentWeeklyOff, Long> {

    List<DepartmentWeeklyOff>
    findByDepartment_DepartmentIdOrderByDayOfWeekAsc(Long departmentId);

    void deleteByDepartment_DepartmentId(Long departmentId);
}