package com.kaisha.payroll.employee.numberseries.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.company.repository.CompanyRepository;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesRequest;
import com.kaisha.payroll.employee.numberseries.dto.NumberSeriesResponse;
import com.kaisha.payroll.employee.numberseries.entity.EmployeeNumberSeries;
import com.kaisha.payroll.employee.numberseries.repository.EmployeeNumberSeriesRepository;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeNumberSeriesService {

    private final EmployeeNumberSeriesRepository seriesRepository;

    private final CompanyRepository companyRepository;

    private final EmployeeRepository employeeRepository;

    private final CurrentUserService currentUserService;

    public EmployeeNumberSeriesService(
            EmployeeNumberSeriesRepository seriesRepository,
            CompanyRepository companyRepository,
            EmployeeRepository employeeRepository,
            CurrentUserService currentUserService
    ) {

        this.seriesRepository =
                seriesRepository;

        this.companyRepository =
                companyRepository;

        this.employeeRepository =
                employeeRepository;

        this.currentUserService =
                currentUserService;
    }

    // ============================================================
    // GET ALL SERIES
    // ============================================================

    @Transactional(readOnly = true)
    public List<NumberSeriesResponse> getAllSeries() {

        String companyId =
                currentUserService.getCurrentCompanyId();

        return seriesRepository
                .findByCompany_CompanyIdOrderByDepartmentAsc(
                        companyId
                )
                .stream()
                .map(NumberSeriesResponse::new)
                .toList();
    }

    // ============================================================
    // GET SINGLE SERIES
    // ============================================================

    @Transactional(readOnly = true)
    public NumberSeriesResponse getSeries(
            Long id
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series =
                seriesRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Number series not found"
                                )
                        );

        return new NumberSeriesResponse(series);
    }

    // ============================================================
    // CREATE SERIES
    // ============================================================

    @Transactional
    public NumberSeriesResponse createSeries(
            NumberSeriesRequest request
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        Company company =
                companyRepository
                        .findById(companyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Company not found"
                                )
                        );

        validateRequest(request);

        String department =
                cleanUpper(request.getDepartment());

        String prefix =
                cleanUpper(request.getPrefix());

        boolean exists =
                seriesRepository
                        .existsByDepartmentIgnoreCaseAndCompany_CompanyId(
                                department,
                                companyId
                        );

        if (exists) {

            throw new RuntimeException(
                    "Number series already exists for department: "
                            + department
            );
        }

        EmployeeNumberSeries series =
                new EmployeeNumberSeries();

        series.setCompany(company);

        series.setDepartment(
                department
        );

        series.setPrefix(
                prefix
        );

        series.setStartNumber(
                request.getStartNumber()
        );

        series.setEndNumber(
                request.getEndNumber()
        );

        series.setNumberLength(
                request.getNumberLength()
        );

        series.setNextNumber(
                request.getStartNumber()
        );

        series.setActive(
                request.getActive() == null
                        ||
                        request.getActive()
        );

        EmployeeNumberSeries saved =
                seriesRepository.save(series);

        return new NumberSeriesResponse(saved);
    }

    // ============================================================
    // UPDATE SERIES
    // ============================================================

    @Transactional
    public NumberSeriesResponse updateSeries(
            Long id,
            NumberSeriesRequest request
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        validateRequest(request);

        EmployeeNumberSeries series =
                seriesRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Number series not found"
                                )
                        );

        String department =
                cleanUpper(request.getDepartment());

        String prefix =
                cleanUpper(request.getPrefix());

        boolean duplicate =
                seriesRepository
                        .existsByDepartmentIgnoreCaseAndCompany_CompanyIdAndIdNot(
                                department,
                                companyId,
                                id
                        );

        if (duplicate) {

            throw new RuntimeException(
                    "Number series already exists for department: "
                            + department
            );
        }

        /*
         * Do not allow an update that would make the
         * already-generated number invalid.
         */
        Long currentNext =
                series.getNextNumber();

        if (
                request.getStartNumber()
                        > currentNext
        ) {

            throw new RuntimeException(
                    "Starting number cannot be greater than the current next number"
            );
        }

        if (
                request.getEndNumber()
                        < currentNext
        ) {

            throw new RuntimeException(
                    "Ending number cannot be less than the current next number"
            );
        }

        series.setDepartment(
                department
        );

        series.setPrefix(
                prefix
        );

        series.setStartNumber(
                request.getStartNumber()
        );

        series.setEndNumber(
                request.getEndNumber()
        );

        series.setNumberLength(
                request.getNumberLength()
        );

        if (request.getActive() != null) {

            series.setActive(
                    request.getActive()
            );
        }

        EmployeeNumberSeries saved =
                seriesRepository.save(series);

        return new NumberSeriesResponse(saved);
    }

    // ============================================================
    // DELETE SERIES
    // ============================================================

    @Transactional
    public void deleteSeries(
            Long id
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series =
                seriesRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Number series not found"
                                )
                        );

        /*
         * Do not delete a series after it has started
         * generating employee IDs.
         *
         * This protects existing employee ID generation.
         */
        if (
                !series.getNextNumber()
                        .equals(series.getStartNumber())
        ) {

            throw new RuntimeException(
                    "Cannot delete a number series after employee IDs have been generated. Deactivate it instead."
            );
        }

        seriesRepository.delete(series);
    }

    // ============================================================
    // GENERATE NEXT EMPLOYEE ID
    //
    // Called by EmployeeService when creating an employee.
    // ============================================================

    @Transactional
    public String generateNextEmployeeId(
            String department
    ) {

        String companyId =
                currentUserService.getCurrentCompanyId();

        EmployeeNumberSeries series =
                seriesRepository
                        .findByDepartmentIgnoreCaseAndCompany_CompanyId(
                                department,
                                companyId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No number series configured for department: "
                                                + department
                                )
                        );

        if (!series.isActive()) {

            throw new RuntimeException(
                    "Number series is inactive for department: "
                            + department
            );
        }

        Long nextNumber =
                series.getNextNumber();

        if (
                nextNumber
                        > series.getEndNumber()
        ) {

            throw new RuntimeException(
                    "Number series limit reached for department: "
                            + department
            );
        }

        String employeeId =
                buildEmployeeId(
                        series.getPrefix(),
                        nextNumber,
                        series.getNumberLength()
                );

        /*
         * Make sure the generated employee ID is not
         * already used by this company.
         */
        if (
                employeeRepository
                        .existsByEmployeeIdAndCompany_CompanyId(
                                employeeId,
                                companyId
                        )
        ) {

            throw new RuntimeException(
                    "Generated employee ID already exists: "
                            + employeeId
            );
        }

        /*
         * Move the series forward.
         */
        series.setNextNumber(
                nextNumber + 1
        );

        seriesRepository.save(series);

        return employeeId;
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateRequest(
            NumberSeriesRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Number series data is required"
            );
        }

        if (
                request.getDepartment() == null
                        ||
                        request.getDepartment()
                                .trim()
                                .isEmpty()
        ) {

            throw new RuntimeException(
                    "Department is required"
            );
        }

        if (
                request.getPrefix() == null
                        ||
                        request.getPrefix()
                                .trim()
                                .isEmpty()
        ) {

            throw new RuntimeException(
                    "Prefix is required"
            );
        }

        if (
                !request.getPrefix()
                        .trim()
                        .matches("[A-Za-z]+")
        ) {

            throw new RuntimeException(
                    "Prefix can contain letters only"
            );
        }

        if (request.getStartNumber() == null) {

            throw new RuntimeException(
                    "Starting number is required"
            );
        }

        if (request.getEndNumber() == null) {

            throw new RuntimeException(
                    "Ending number is required"
            );
        }

        if (
                request.getStartNumber()
                        < 0
        ) {

            throw new RuntimeException(
                    "Starting number cannot be negative"
            );
        }

        if (
                request.getEndNumber()
                        < 0
        ) {

            throw new RuntimeException(
                    "Ending number cannot be negative"
            );
        }

        if (
                request.getStartNumber()
                        > request.getEndNumber()
        ) {

            throw new RuntimeException(
                    "Starting number cannot be greater than ending number"
            );
        }

        if (request.getNumberLength() == null) {

            throw new RuntimeException(
                    "Number length is required"
            );
        }

        if (
                request.getNumberLength()
                        < 1
                        ||
                        request.getNumberLength()
                                > 10
        ) {

            throw new RuntimeException(
                    "Number length must be between 1 and 10"
            );
        }

        long maximumForLength =
                calculateMaximumNumber(
                        request.getNumberLength()
                );

        if (
                request.getEndNumber()
                        > maximumForLength
        ) {

            throw new RuntimeException(
                    "Ending number does not fit within the selected number length"
            );
        }
    }

    // ============================================================
    // BUILD EMPLOYEE ID
    // ============================================================

    private String buildEmployeeId(
            String prefix,
            Long number,
            Integer numberLength
    ) {

        return prefix +
                String.format(
                        "%0" + numberLength + "d",
                        number
                );
    }

    // ============================================================
    // MAX NUMBER FOR LENGTH
    // ============================================================

    private long calculateMaximumNumber(
            int length
    ) {

        long maximum = 1;

        for (int i = 0; i < length; i++) {

            maximum *= 10;
        }

        return maximum - 1;
    }

    // ============================================================
    // CLEAN STRING
    // ============================================================

    private String cleanUpper(
            String value
    ) {

        return value
                .trim()
                .toUpperCase();
    }
}