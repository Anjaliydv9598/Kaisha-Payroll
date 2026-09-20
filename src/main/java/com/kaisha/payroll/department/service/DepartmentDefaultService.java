package com.kaisha.payroll.department.service;

import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.entity.Position;
import com.kaisha.payroll.department.repository.DepartmentRepository;
import com.kaisha.payroll.department.repository.PositionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class DepartmentDefaultService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public DepartmentDefaultService(
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository) {

        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public void createDefaultDepartments(
            Company company) {

        String[] departments = {
                "HR",
                "Finance",
                "PP",
                "Sales",
                "Operation",
                "Marketing",
                "IT"
        };

        for (String departmentName : departments) {

            Department department =
                    new Department();

            department.setCompany(company);
            department.setDepartmentName(
                    departmentName
            );
            department.setActive(true);

            department =
                    departmentRepository.save(
                            department
                    );

            createPosition(
                    department,
                    "General Manager"
            );

            createPosition(
                    department,
                    "Deputy Manager"
            );

            createPosition(
                    department,
                    "Supervisor " + departmentName
            );

            createPosition(
                    department,
                    "Employees"
            );
        }
    }

    private void createPosition(
            Department department,
            String name) {

        Position position =
                new Position();

        position.setDepartment(department);
        position.setPositionName(name);
        position.setActive(true);

        positionRepository.save(position);
    }
}