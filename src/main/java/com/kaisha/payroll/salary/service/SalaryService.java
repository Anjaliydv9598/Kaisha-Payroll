package com.kaisha.payroll.salary.service;

import com.kaisha.payroll.salary.dto.SalaryComponentDto;
import com.kaisha.payroll.salary.dto.SalaryRequest;
import com.kaisha.payroll.salary.dto.SalaryResponse;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.entity.SalaryComponent;
import com.kaisha.payroll.salary.repository.SalaryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class SalaryService {

    private final SalaryRepository salaryRepository;

    public SalaryService(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    // =====================================================
    // CREATE
    // =====================================================

    public SalaryResponse create(SalaryRequest request) {

        validateRequest(request);

        String employeeId =
                request.getEmployeeId().trim();

        String payPeriod =
                request.getPayPeriod().trim();

        if (salaryRepository.existsByEmployeeIdAndPayPeriod(
                employeeId,
                payPeriod)) {

            throw new RuntimeException(
                    "Salary already exists for employee "
                            + employeeId
                            + " for "
                            + payPeriod
            );
        }

        Salary salary = new Salary();

        salary.setEmployeeId(employeeId);
        salary.setPayPeriod(payPeriod);

        LocalDateTime now = LocalDateTime.now();

        salary.setCreatedAt(now);
        salary.setUpdatedAt(now);

        replaceComponents(
                salary,
                request.getComponents()
        );

        calculateTotals(salary);

        Salary saved =
                salaryRepository.save(salary);

        return toResponse(saved);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public SalaryResponse update(
            Long salaryId,
            SalaryRequest request) {

        validateRequest(request);

        if (salaryId == null) {
            throw new RuntimeException(
                    "Salary ID is required"
            );
        }

        Salary salary =
                salaryRepository.findById(salaryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Salary record not found"
                                )
                        );

        String employeeId =
                request.getEmployeeId().trim();

        String payPeriod =
                request.getPayPeriod().trim();

        boolean duplicate =
                salaryRepository
                        .existsByEmployeeIdAndPayPeriodAndSalaryIdNot(
                                employeeId,
                                payPeriod,
                                salaryId
                        );

        if (duplicate) {
            throw new RuntimeException(
                    "Another salary record already exists "
                            + "for this employee and pay period."
            );
        }

        salary.setEmployeeId(employeeId);
        salary.setPayPeriod(payPeriod);

        // Remove old components
        salary.clearComponents();

        // Add new components
        replaceComponents(
                salary,
                request.getComponents()
        );

        // Recalculate
        calculateTotals(salary);

        // Update timestamp
        salary.setUpdatedAt(
                LocalDateTime.now()
        );

        Salary saved =
                salaryRepository.save(salary);

        return toResponse(saved);
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getAll() {

        return salaryRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public SalaryResponse getById(Long salaryId) {

        if (salaryId == null) {
            throw new RuntimeException(
                    "Salary ID is required"
            );
        }

        Salary salary =
                salaryRepository.findById(salaryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Salary record not found"
                                )
                        );

        return toResponse(salary);
    }

    // =====================================================
    // GET BY EMPLOYEE
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getByEmployee(
            String employeeId) {

        if (employeeId == null ||
                employeeId.trim().isEmpty()) {

            throw new RuntimeException(
                    "Employee ID is required"
            );
        }

        return salaryRepository
                .findByEmployeeIdOrderByPayPeriodDesc(
                        employeeId.trim()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY PERIOD
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getByPeriod(
            String payPeriod) {

        if (payPeriod == null ||
                !payPeriod.trim()
                        .matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Pay period must be in YYYY-MM format"
            );
        }

        return salaryRepository
                .findByPayPeriodOrderByEmployeeIdAsc(
                        payPeriod.trim()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // DELETE
    // =====================================================

    public void delete(Long salaryId) {

        if (salaryId == null) {
            throw new RuntimeException(
                    "Salary ID is required"
            );
        }

        Salary salary =
                salaryRepository.findById(salaryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Salary record not found"
                                )
                        );

        salaryRepository.delete(salary);
    }

    // =====================================================
    // REPLACE COMPONENTS
    // =====================================================

    private void replaceComponents(
            Salary salary,
            List<SalaryComponentDto> dtos) {

        if (salary == null) {
            throw new RuntimeException(
                    "Salary is required"
            );
        }

        if (dtos == null) {
            return;
        }

        for (SalaryComponentDto dto : dtos) {

            if (dto == null) {
                continue;
            }

            // -------------------------------------------------
            // Component Name
            // -------------------------------------------------

            String name =
                    dto.getComponentName();

            if (name == null ||
                    name.trim().isEmpty()) {

                throw new RuntimeException(
                        "Salary component name is required"
                );
            }

            name = name.trim();

            // -------------------------------------------------
            // Amount
            // -------------------------------------------------

            BigDecimal amount =
                    dto.getAmount();

            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            if (amount.compareTo(
                    BigDecimal.ZERO) < 0) {

                throw new RuntimeException(
                        "Salary component amount "
                                + "cannot be negative"
                );
            }

            // -------------------------------------------------
            // Component Type
            // -------------------------------------------------

            if (dto.getComponentType() == null) {

                throw new RuntimeException(
                        "Salary component type is required "
                                + "for: "
                                + name
                );
            }

            // -------------------------------------------------
            // Create Component
            // -------------------------------------------------

            SalaryComponent component =
                    new SalaryComponent();

            component.setComponentName(name);

            component.setComponentType(
                    dto.getComponentType()
            );

            component.setAmount(amount);

            component.setSystemDefined(
                    dto.isSystemDefined()
            );

            // -------------------------------------------------
            // Attach to Salary
            // -------------------------------------------------

            salary.addComponent(component);
        }
    }

    // =====================================================
    // CALCULATE TOTALS
    // =====================================================

    private void calculateTotals(
            Salary salary) {

        BigDecimal gross =
                BigDecimal.ZERO;

        BigDecimal deductions =
                BigDecimal.ZERO;

        List<SalaryComponent> components =
                salary.getComponents();

        if (components == null) {
            components =
                    Collections.emptyList();
        }

        for (SalaryComponent component :
                components) {

            if (component == null) {
                continue;
            }

            BigDecimal amount =
                    component.getAmount();

            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            SalaryComponent.ComponentType type =
                    component.getComponentType();

            if (type ==
                    SalaryComponent.ComponentType.EARNING) {

                gross =
                        gross.add(amount);

            } else if (type ==
                    SalaryComponent.ComponentType.DEDUCTION) {

                deductions =
                        deductions.add(amount);
            }
        }

        BigDecimal net =
                gross.subtract(deductions);

        salary.setGrossSalary(gross);

        salary.setTotalDeductions(
                deductions
        );

        salary.setNetSalary(net);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRequest(
            SalaryRequest request) {

        if (request == null) {

            throw new RuntimeException(
                    "Salary request is required"
            );
        }

        // Employee ID
        if (request.getEmployeeId() == null ||
                request.getEmployeeId()
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "Employee ID is required"
            );
        }

        // Pay Period
        if (request.getPayPeriod() == null ||
                !request.getPayPeriod()
                        .trim()
                        .matches("\\d{4}-\\d{2}")) {

            throw new RuntimeException(
                    "Pay period must be in YYYY-MM format"
            );
        }

        String payPeriod =
                request.getPayPeriod().trim();

        // Validate month
        try {

            int month =
                    Integer.parseInt(
                            payPeriod.substring(5, 7)
                    );

            if (month < 1 || month > 12) {

                throw new RuntimeException(
                        "Pay period month must be between 01 and 12"
                );
            }

        } catch (NumberFormatException e) {

            throw new RuntimeException(
                    "Invalid pay period"
            );
        }

        // Validate components
        if (request.getComponents() != null) {

            for (SalaryComponentDto dto :
                    request.getComponents()) {

                if (dto == null) {
                    continue;
                }

                if (dto.getComponentName() == null ||
                        dto.getComponentName()
                                .trim()
                                .isEmpty()) {

                    throw new RuntimeException(
                            "Salary component name is required"
                    );
                }

                if (dto.getComponentType() == null) {

                    throw new RuntimeException(
                            "Salary component type is required"
                    );
                }

                if (dto.getAmount() != null &&
                        dto.getAmount()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0) {

                    throw new RuntimeException(
                            "Salary component amount "
                                    + "cannot be negative"
                    );
                }
            }
        }
    }

    // =====================================================
    // RESPONSE MAPPING
    // =====================================================

    private SalaryResponse toResponse(
            Salary salary) {

        SalaryResponse response =
                new SalaryResponse();

        response.setSalaryId(
                salary.getSalaryId()
        );

        response.setEmployeeId(
                salary.getEmployeeId()
        );

        response.setPayPeriod(
                salary.getPayPeriod()
        );

        response.setGrossSalary(
                salary.getGrossSalary()
        );

        response.setTotalDeductions(
                salary.getTotalDeductions()
        );

        response.setNetSalary(
                salary.getNetSalary()
        );

        List<SalaryComponent> components =
                salary.getComponents();

        if (components == null) {

            response.setComponents(
                    Collections.emptyList()
            );

        } else {

            response.setComponents(
                    components
                            .stream()
                            .map(this::toComponentDto)
                            .toList()
            );
        }

        response.setCreatedAt(
                salary.getCreatedAt()
        );

        response.setUpdatedAt(
                salary.getUpdatedAt()
        );

        return response;
    }

    // =====================================================
    // COMPONENT DTO MAPPING
    // =====================================================

    private SalaryComponentDto toComponentDto(
            SalaryComponent component) {

        SalaryComponentDto dto =
                new SalaryComponentDto();

        dto.setId(
                component.getId()
        );

        dto.setComponentName(
                component.getComponentName()
        );

        dto.setComponentType(
                component.getComponentType()
        );

        dto.setAmount(
                component.getAmount()
        );

        dto.setSystemDefined(
                component.isSystemDefined()
        );

        return dto;
    }
}