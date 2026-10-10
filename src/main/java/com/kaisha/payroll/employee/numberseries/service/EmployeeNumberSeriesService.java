package com.kaisha.payroll.employee.numberseries.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesRequest;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesResponse;
import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;
import com.kaisha.payroll.employee.numberseries.repository.EmployeeNumberSeriesRepository;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeNumberSeriesService {

    private final EmployeeNumberSeriesRepository seriesRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFieldRepository employeeFieldRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<NumberSeriesResponse> getAllSeries() {
        String companyId = currentUserService.getCurrentCompanyId();

        return seriesRepository
                .findByCompany_CompanyIdOrderByDepartmentAsc(companyId)
                .stream()
                .map(NumberSeriesResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NumberSeriesResponse getSeries(Long id) {
        String companyId = currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series = seriesRepository
                .findByIdAndCompany_CompanyId(id, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Number series not found"));

        return new NumberSeriesResponse(series);
    }

    @Transactional
    public NumberSeriesResponse createSeries(NumberSeriesRequest request) {
        validateRequest(request);

        String companyId = currentUserService.getCurrentCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        String department = cleanUpper(request.getDepartment());
        String prefix = cleanUpper(request.getPrefix());

        if (seriesRepository
                .existsByDepartmentIgnoreCaseAndCompany_CompanyId(
                        department, companyId)) {
            throw new RuntimeException(
                    "A series already exists for department " + department);
        }

        long start = request.getStartNumber();
        long end = request.getEndNumber();

        if (end - start + 1 > 10000) {
            throw new RuntimeException(
                    "A maximum of 10000 IDs can be reserved at one time");
        }

        // Pre-check all IDs before saving any records.
        for (long number = start; number <= end; number++) {
            String employeeId = buildEmployeeId(
                    prefix, number, request.getNumberLength());

            if (employeeRepository.existsByEmployeeIdAndCompany_CompanyId(
                    employeeId, companyId)) {
                throw new RuntimeException(
                        "Employee ID already exists: " + employeeId);
            }
        }

        EmployeeNumberSeries series = new EmployeeNumberSeries();
        series.setCompany(company);
        series.setDepartment(department);
        series.setPrefix(prefix);
        series.setStartNumber(start);
        series.setEndNumber(end);
        series.setNumberLength(request.getNumberLength());
        series.setNextNumber(end + 1);
        series.setActive(request.getActive() == null || request.getActive());

        EmployeeNumberSeries savedSeries = seriesRepository.save(series);

        List<Employee> employees = new ArrayList<Employee>();

        for (long number = start; number <= end; number++) {
            Employee employee = new Employee();
            employee.setEmployeeId(
                    buildEmployeeId(prefix, number,
                            request.getNumberLength()));
            employee.setCompany(company);
            employee.setActive(true);
            employee.setDetailsCompleted(false);
            employees.add(employee);
        }

        List<Employee> savedEmployees =
                employeeRepository.saveAll(employees);

        List<EmployeeField> departmentFields = new ArrayList<EmployeeField>();

        for (Employee employee : savedEmployees) {
            EmployeeField field = new EmployeeField();
            field.setEmployee(employee);
            field.setFieldName("Department");
            field.setFieldValue(department);
            departmentFields.add(field);
        }

        employeeFieldRepository.saveAll(departmentFields);

        return new NumberSeriesResponse(savedSeries);
    }

    @Transactional
    public NumberSeriesResponse updateSeries(
            Long id, NumberSeriesRequest request) {

        validateRequest(request);

        String companyId = currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series = seriesRepository
                .findByIdAndCompany_CompanyId(id, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Number series not found"));

        boolean unchanged =
                series.getDepartment().equalsIgnoreCase(
                        cleanUpper(request.getDepartment()))
                        && series.getPrefix().equalsIgnoreCase(
                        cleanUpper(request.getPrefix()))
                        && series.getStartNumber().equals(request.getStartNumber())
                        && series.getEndNumber().equals(request.getEndNumber())
                        && series.getNumberLength().equals(request.getNumberLength());

        if (!unchanged) {
            throw new RuntimeException(
                    "A reserved series cannot be remapped. "
                            + "Only its active status can be changed.");
        }

        if (request.getActive() != null) {
            series.setActive(request.getActive());
        }

        return new NumberSeriesResponse(seriesRepository.save(series));
    }

    @Transactional
    public void deleteSeries(Long id) {
        String companyId = currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series = seriesRepository
                .findByIdAndCompany_CompanyId(id, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Number series not found"));

        throw new RuntimeException(
                "Reserved IDs must remain protected. Deactivate this series instead.");
    }

    /*
     * Kept so older code still compiles, but creating a new employee by
     * generating another ID is no longer allowed. Complete a reserved ID.
     */
    @Transactional
    public String generateNextEmployeeId(String department) {
        throw new RuntimeException(
                "Select an existing reserved employee ID and complete its details.");
    }

    private void validateRequest(NumberSeriesRequest request) {
        if (request == null) {
            throw new RuntimeException("Number series data is required");
        }

        if (request.getDepartment() == null
                || request.getDepartment().trim().isEmpty()) {
            throw new RuntimeException("Department is required");
        }

        if (request.getPrefix() == null
                || !request.getPrefix().trim().matches("[A-Za-z]+")) {
            throw new RuntimeException("Prefix must contain letters only");
        }

        if (request.getStartNumber() == null
                || request.getEndNumber() == null) {
            throw new RuntimeException("Start and end numbers are required");
        }

        if (request.getStartNumber() < 0
                || request.getEndNumber() < request.getStartNumber()) {
            throw new RuntimeException("Invalid number range");
        }

        if (request.getNumberLength() == null
                || request.getNumberLength() < 1
                || request.getNumberLength() > 10) {
            throw new RuntimeException(
                    "Number length must be between 1 and 10");
        }
    }

    private String buildEmployeeId(
            String prefix, long number, int numberLength) {
        return prefix + String.format("%0" + numberLength + "d", number);
    }

    private String cleanUpper(String value) {
        return value.trim().toUpperCase();
    }
}