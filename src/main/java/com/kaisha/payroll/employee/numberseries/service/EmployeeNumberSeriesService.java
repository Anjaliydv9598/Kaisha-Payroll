
package com.kaisha.payroll.employee.numberseries.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.repository.DepartmentRepository;
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
    private final DepartmentRepository departmentRepository;

    // =========================================================
    // GET ALL NUMBER SERIES
    // =========================================================

    @Transactional(readOnly = true)
    public List<NumberSeriesResponse> getAllSeries() {

        String companyId = currentUserService.getCurrentCompanyId();

        return seriesRepository
                .findByCompany_CompanyIdOrderByDepartmentAsc(companyId)
                .stream()
                .map(NumberSeriesResponse::new)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET NUMBER SERIES BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public NumberSeriesResponse getSeries(Long id) {

        String companyId = currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series = seriesRepository
                .findByIdAndCompany_CompanyId(id, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Number series not found"));

        return new NumberSeriesResponse(series);
    }

    // =========================================================
    // CREATE NUMBER SERIES
    // =========================================================

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

        // Check all employee IDs before saving.
        for (long number = start; number <= end; number++) {

            String employeeId = buildEmployeeId(
                    prefix,
                    number,
                    request.getNumberLength()
            );

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

        EmployeeNumberSeries savedSeries =
                seriesRepository.save(series);

        // Synchronize Department with the saved Number Series.
        syncDepartment(
                company,
                companyId,
                department,
                savedSeries.isActive()
        );

        // Create reserved employee records.
        List<Employee> employees = new ArrayList<>();

        for (long number = start; number <= end; number++) {

            Employee employee = new Employee();

            employee.setEmployeeId(
                    buildEmployeeId(
                            prefix,
                            number,
                            request.getNumberLength()
                    )
            );

            employee.setCompany(company);
            employee.setActive(true);
            employee.setDetailsCompleted(false);

            employees.add(employee);
        }

        List<Employee> savedEmployees =
                employeeRepository.saveAll(employees);

        // Save Department field for each reserved employee.
        List<EmployeeField> departmentFields = new ArrayList<>();

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

    // =========================================================
    // UPDATE NUMBER SERIES
    // =========================================================

    @Transactional
    public NumberSeriesResponse updateSeries(
            Long id,
            NumberSeriesRequest request) {

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
                        && series.getStartNumber().equals(
                        request.getStartNumber())
                        && series.getEndNumber().equals(
                        request.getEndNumber())
                        && series.getNumberLength().equals(
                        request.getNumberLength());

        if (!unchanged) {
            throw new RuntimeException(
                    "A reserved series cannot be remapped. "
                            + "Only its active status can be changed.");
        }

        if (request.getActive() != null) {
            series.setActive(request.getActive());
        }

        EmployeeNumberSeries updatedSeries =
                seriesRepository.save(series);

        // Synchronize Department active status.
        syncDepartment(
                updatedSeries.getCompany(),
                companyId,
                updatedSeries.getDepartment(),
                updatedSeries.isActive()
        );

        return new NumberSeriesResponse(updatedSeries);
    }

    // =========================================================
    // DELETE NUMBER SERIES
    // =========================================================

    @Transactional
    public void deleteSeries(Long id) {

        String companyId = currentUserService.getCurrentCompanyId();

        seriesRepository
                .findByIdAndCompany_CompanyId(id, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Number series not found"));

        throw new RuntimeException(
                "Reserved IDs must remain protected. Deactivate this series instead.");
    }

    // =========================================================
    // SYNCHRONIZE DEPARTMENT WITH NUMBER SERIES
    // =========================================================

    private void syncDepartment(
            Company company,
            String companyId,
            String departmentName,
            boolean active) {

        Department department = departmentRepository
                .findByCompany_CompanyIdAndDepartmentNameIgnoreCase(
                        companyId,
                        departmentName
                )
                .orElse(null);

        if (department != null) {

            department.setActive(active);
            departmentRepository.save(department);
            return;
        }

        // Do not create inactive departments.
        if (!active) {
            return;
        }

        Department newDepartment = new Department();

        newDepartment.setCompany(company);
        newDepartment.setDepartmentName(departmentName);
        newDepartment.setActive(true);

        departmentRepository.save(newDepartment);
    }

    // =========================================================
    // ONE-TIME SYNCHRONIZATION FOR EXISTING NUMBER SERIES
    // =========================================================

    @Transactional
    public void syncExistingSeriesWithDepartments() {

        String companyId = currentUserService.getCurrentCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new RuntimeException("Company not found"));

        List<EmployeeNumberSeries> seriesList =
                seriesRepository
                        .findByCompany_CompanyIdOrderByDepartmentAsc(
                                companyId
                        );

        for (EmployeeNumberSeries series : seriesList) {

            syncDepartment(
                    company,
                    companyId,
                    series.getDepartment(),
                    series.isActive()
            );
        }
    }

    // =========================================================
    // EMPLOYEE ID GENERATION BLOCK
    // =========================================================

    @Transactional
    public String generateNextEmployeeId(String department) {

        throw new RuntimeException(
                "Select an existing reserved employee ID and complete its details.");
    }

    // =========================================================
    // VALIDATION
    // =========================================================

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

            throw new RuntimeException(
                    "Prefix must contain letters only");
        }

        if (request.getStartNumber() == null
                || request.getEndNumber() == null) {

            throw new RuntimeException(
                    "Start and end numbers are required");
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

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String buildEmployeeId(
            String prefix,
            long number,
            int numberLength) {

        return prefix + String.format(
                "%0" + numberLength + "d",
                number
        );
    }

    private String cleanUpper(String value) {
        return value.trim().toUpperCase();
    }
}
