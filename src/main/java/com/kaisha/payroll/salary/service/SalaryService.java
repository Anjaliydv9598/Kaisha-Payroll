
package com.kaisha.payroll.salary.service;

import com.kaisha.payroll.salary.dto.SalaryComponentDto;
import com.kaisha.payroll.salary.dto.SalaryRequest;
import com.kaisha.payroll.salary.dto.SalaryResponse;
import com.kaisha.payroll.salary.entity.Salary;
import com.kaisha.payroll.salary.entity.SalaryComponent;
import com.kaisha.payroll.salary.repository.SalaryRepository;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.employee.repository.EmployeeRepository;

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
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;

    public SalaryService(
            SalaryRepository salaryRepository,
            CurrentUserService currentUserService,
            EmployeeRepository employeeRepository
    ) {
        this.salaryRepository = salaryRepository;
        this.currentUserService = currentUserService;
        this.employeeRepository = employeeRepository;
    }

    private String getCompanyId() {
        return currentUserService.getCurrentCompanyId();
    }

    private void validateEmployeeForCompany(
            String employeeId,
            String companyId
    ) {
        employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        employeeId,
                        companyId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found or inactive in your company: "
                                        + employeeId
                        )
                );
    }

    private Salary findSalaryForCompany(
            Long salaryId,
            String companyId
    ) {
        if (salaryId == null) {
            throw new RuntimeException("Salary ID is required");
        }

        return salaryRepository
                .findBySalaryIdAndCompanyId(salaryId, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Salary record not found")
                );
    }

    // =====================================================
    // CREATE
    // =====================================================

    public SalaryResponse create(SalaryRequest request) {

        validateRequest(request);

        String companyId = getCompanyId();
        String employeeId = request.getEmployeeId().trim();
        String payPeriod = request.getPayPeriod().trim();

        validateEmployeeForCompany(employeeId, companyId);

        if (salaryRepository
                .existsByCompanyIdAndEmployeeIdAndPayPeriod(
                        companyId,
                        employeeId,
                        payPeriod
                )) {
            throw new RuntimeException(
                    "Salary already exists for employee "
                            + employeeId + " for " + payPeriod
            );
        }

        Salary salary = new Salary();

        salary.setCompanyId(companyId);
        salary.setEmployeeId(employeeId);
        salary.setPayPeriod(payPeriod);

        LocalDateTime now = LocalDateTime.now();
        salary.setCreatedAt(now);
        salary.setUpdatedAt(now);

        replaceComponents(salary, request.getComponents());
        calculateTotals(salary);

        return toResponse(salaryRepository.save(salary));
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public SalaryResponse update(
            Long salaryId,
            SalaryRequest request
    ) {
        validateRequest(request);

        String companyId = getCompanyId();

        Salary salary = findSalaryForCompany(salaryId, companyId);

        String employeeId = request.getEmployeeId().trim();
        String payPeriod = request.getPayPeriod().trim();

        validateEmployeeForCompany(employeeId, companyId);

        boolean duplicate =
                salaryRepository
                        .existsByCompanyIdAndEmployeeIdAndPayPeriodAndSalaryIdNot(
                                companyId,
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

        salary.clearComponents();
        replaceComponents(salary, request.getComponents());

        calculateTotals(salary);

        salary.setUpdatedAt(LocalDateTime.now());

        return toResponse(salaryRepository.save(salary));
    }

    // =====================================================
    // GET ALL — CURRENT COMPANY
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getAll() {

        String companyId = getCompanyId();

        return salaryRepository
                .findByCompanyIdOrderByPayPeriodDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY ID — CURRENT COMPANY
    // =====================================================

    @Transactional(readOnly = true)
    public SalaryResponse getById(Long salaryId) {

        String companyId = getCompanyId();

        return toResponse(findSalaryForCompany(salaryId, companyId));
    }

    // =====================================================
    // GET BY EMPLOYEE — CURRENT COMPANY
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getByEmployee(String employeeId) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required");
        }

        String companyId = getCompanyId();
        String normalizedEmployeeId = employeeId.trim();

        validateEmployeeForCompany(normalizedEmployeeId, companyId);

        return salaryRepository
                .findByCompanyIdAndEmployeeIdOrderByPayPeriodDesc(
                        companyId,
                        normalizedEmployeeId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET BY PERIOD — CURRENT COMPANY
    // =====================================================

    @Transactional(readOnly = true)
    public List<SalaryResponse> getByPeriod(String payPeriod) {

        validatePayPeriod(payPeriod);

        String companyId = getCompanyId();

        return salaryRepository
                .findByCompanyIdAndPayPeriodOrderByEmployeeIdAsc(
                        companyId,
                        payPeriod.trim()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // DELETE — CURRENT COMPANY
    // =====================================================

    public void delete(Long salaryId) {

        String companyId = getCompanyId();

        Salary salary = findSalaryForCompany(salaryId, companyId);

        salaryRepository.delete(salary);
    }

    // =====================================================
    // REPLACE COMPONENTS
    // =====================================================

    private void replaceComponents(
            Salary salary,
            List<SalaryComponentDto> dtos
    ) {
        if (salary == null) {
            throw new RuntimeException("Salary is required");
        }

        if (dtos == null) {
            return;
        }

        for (SalaryComponentDto dto : dtos) {

            if (dto == null) {
                continue;
            }

            String name = dto.getComponentName();

            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException(
                        "Salary component name is required"
                );
            }

            name = name.trim();

            BigDecimal amount = dto.getAmount();

            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Salary component amount cannot be negative"
                );
            }

            if (dto.getComponentType() == null) {
                throw new RuntimeException(
                        "Salary component type is required for: " + name
                );
            }

            SalaryComponent component = new SalaryComponent();

            component.setComponentName(name);
            component.setComponentType(dto.getComponentType());
            component.setAmount(amount);
            component.setSystemDefined(dto.isSystemDefined());

            salary.addComponent(component);
        }
    }

    // =====================================================
    // CALCULATE TOTALS
    // =====================================================

    private void calculateTotals(Salary salary) {

        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;

        List<SalaryComponent> components = salary.getComponents();

        if (components == null) {
            components = Collections.emptyList();
        }

        for (SalaryComponent component : components) {

            if (component == null) {
                continue;
            }

            BigDecimal amount = component.getAmount();

            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            SalaryComponent.ComponentType type =
                    component.getComponentType();

            if (type == SalaryComponent.ComponentType.EARNING) {
                gross = gross.add(amount);
            } else if (type == SalaryComponent.ComponentType.DEDUCTION) {
                deductions = deductions.add(amount);
            }
        }

        salary.setGrossSalary(gross);
        salary.setTotalDeductions(deductions);
        salary.setNetSalary(gross.subtract(deductions));
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateRequest(SalaryRequest request) {

        if (request == null) {
            throw new RuntimeException("Salary request is required");
        }

        if (request.getEmployeeId() == null ||
                request.getEmployeeId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required");
        }

        validatePayPeriod(request.getPayPeriod());

        if (request.getComponents() != null) {

            for (SalaryComponentDto dto : request.getComponents()) {

                if (dto == null) {
                    continue;
                }

                if (dto.getComponentName() == null ||
                        dto.getComponentName().trim().isEmpty()) {
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
                        dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException(
                            "Salary component amount cannot be negative"
                    );
                }
            }
        }
    }

    private void validatePayPeriod(String payPeriod) {

        if (payPeriod == null ||
                !payPeriod.trim().matches("\\d{4}-\\d{2}")) {
            throw new RuntimeException(
                    "Pay period must be in YYYY-MM format"
            );
        }

        int month = Integer.parseInt(payPeriod.trim().substring(5, 7));

        if (month < 1 || month > 12) {
            throw new RuntimeException(
                    "Pay period month must be between 01 and 12"
            );
        }
    }

    // =====================================================
    // RESPONSE MAPPING
    // =====================================================

    private SalaryResponse toResponse(Salary salary) {

        SalaryResponse response = new SalaryResponse();

        response.setSalaryId(salary.getSalaryId());
        response.setEmployeeId(salary.getEmployeeId());
        response.setPayPeriod(salary.getPayPeriod());
        response.setGrossSalary(salary.getGrossSalary());
        response.setTotalDeductions(salary.getTotalDeductions());
        response.setNetSalary(salary.getNetSalary());

        List<SalaryComponent> components = salary.getComponents();

        if (components == null) {
            response.setComponents(Collections.emptyList());
        } else {
            response.setComponents(
                    components.stream()
                            .map(this::toComponentDto)
                            .toList()
            );
        }

        response.setCreatedAt(salary.getCreatedAt());
        response.setUpdatedAt(salary.getUpdatedAt());

        return response;
    }

    // =====================================================
    // COMPONENT DTO MAPPING
    // =====================================================

    private SalaryComponentDto toComponentDto(
            SalaryComponent component
    ) {
        SalaryComponentDto dto = new SalaryComponentDto();

        dto.setId(component.getId());
        dto.setComponentName(component.getComponentName());
        dto.setComponentType(component.getComponentType());
        dto.setAmount(component.getAmount());
        dto.setSystemDefined(component.isSystemDefined());

        return dto;
    }
}
