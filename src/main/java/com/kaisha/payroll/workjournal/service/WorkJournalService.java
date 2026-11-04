
package com.kaisha.payroll.workjournal.service;

import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.company.entity.Company;

import com.kaisha.payroll.workjournal.dto.WorkJournalRequest;
import com.kaisha.payroll.workjournal.dto.WorkJournalResponse;
import com.kaisha.payroll.workjournal.entity.EmployeeWorkLog;
import com.kaisha.payroll.workjournal.repository.EmployeeWorkLogRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WorkJournalService {

    private final EmployeeWorkLogRepository workLogRepository;
    private final CurrentUserService currentUserService;

    public WorkJournalService(
            EmployeeWorkLogRepository workLogRepository,
            CurrentUserService currentUserService
    ) {
        this.workLogRepository = workLogRepository;
        this.currentUserService = currentUserService;
    }

    // ---------------------------------------------------------
    // GET CURRENT STAFF DETAILS
    // ---------------------------------------------------------

    private User getStaffUser() {

        User user = currentUserService.getCurrentUser();

        if (user.getEmployee() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No employee is linked to this user account."
            );
        }

        if (user.getCompany() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No company is linked to this user account."
            );
        }

        if (!user.isActive() || !user.getEmployee().isActive()) {
            throw new AccessDeniedException(
                    "Inactive staff accounts cannot access the work journal."
            );
        }

        return user;
    }

    // ---------------------------------------------------------
    // SAVE DAILY WORK ENTRY
    // ---------------------------------------------------------

    public WorkJournalResponse createEntry(
            WorkJournalRequest request
    ) {

        User user = getStaffUser();

        if (request.getWorkDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Future work entries are not allowed."
            );
        }

        Employee employee = user.getEmployee();
        Company company = user.getCompany();

        EmployeeWorkLog entry = new EmployeeWorkLog();

        entry.setEmployee(employee);
        entry.setCompany(company);
        entry.setWorkDate(request.getWorkDate());
        entry.setWorkDescription(
                request.getWorkDescription().trim()
        );
        entry.setHighlight(request.isHighlight());

        entry.setHighlightCategory(
                request.isHighlight()
                        ? normalizeCategory(request.getHighlightCategory())
                        : null
        );

        EmployeeWorkLog saved =
                workLogRepository.save(entry);

        return convertToResponse(saved);
    }

    // ---------------------------------------------------------
    // GET ALL MY WORK ENTRIES
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public List<WorkJournalResponse> getMyEntries() {

        User user = getStaffUser();

        return workLogRepository
                .findByEmployee_IdAndCompany_CompanyIdOrderByWorkDateDescCreatedAtDesc(
                        user.getEmployee().getId(),
                        user.getCompany().getCompanyId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET ENTRY BY ID
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public WorkJournalResponse getEntryById(Long id) {

        User user = getStaffUser();

        EmployeeWorkLog entry = findOwnedEntry(id, user);

        return convertToResponse(entry);
    }

    // ---------------------------------------------------------
    // UPDATE MY ENTRY
    // ---------------------------------------------------------

    public WorkJournalResponse updateEntry(
            Long id,
            WorkJournalRequest request
    ) {

        User user = getStaffUser();

        if (request.getWorkDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Future work entries are not allowed."
            );
        }

        EmployeeWorkLog entry = findOwnedEntry(id, user);

        entry.setWorkDate(request.getWorkDate());
        entry.setWorkDescription(
                request.getWorkDescription().trim()
        );
        entry.setHighlight(request.isHighlight());

        entry.setHighlightCategory(
                request.isHighlight()
                        ? normalizeCategory(request.getHighlightCategory())
                        : null
        );

        EmployeeWorkLog updated =
                workLogRepository.save(entry);

        return convertToResponse(updated);
    }

    // ---------------------------------------------------------
    // DELETE MY ENTRY
    // ---------------------------------------------------------

    public void deleteEntry(Long id) {

        User user = getStaffUser();

        EmployeeWorkLog entry = findOwnedEntry(id, user);

        workLogRepository.delete(entry);
    }

    // ---------------------------------------------------------
    // GET MY HIGHLIGHTS
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public List<WorkJournalResponse> getMyHighlights() {

        User user = getStaffUser();

        return workLogRepository
                .findByEmployee_IdAndCompany_CompanyIdAndHighlightTrueOrderByWorkDateDesc(
                        user.getEmployee().getId(),
                        user.getCompany().getCompanyId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET ENTRIES BY DATE RANGE
    // ---------------------------------------------------------

    @Transactional(readOnly = true)
    public List<WorkJournalResponse> getEntriesByDateRange(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null || to == null || from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date range."
            );
        }

        User user = getStaffUser();

        return workLogRepository
                .findByEmployee_IdAndCompany_CompanyIdAndWorkDateBetweenOrderByWorkDateDesc(
                        user.getEmployee().getId(),
                        user.getCompany().getCompanyId(),
                        from,
                        to
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // OWNERSHIP VALIDATION
    // ---------------------------------------------------------

    private EmployeeWorkLog findOwnedEntry(
            Long id,
            User user
    ) {

        return workLogRepository
                .findByIdAndEmployee_IdAndCompany_CompanyId(
                        id,
                        user.getEmployee().getId(),
                        user.getCompany().getCompanyId()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Work journal entry not found."
                        )
                );
    }

    // ---------------------------------------------------------
    // CONVERT ENTITY TO RESPONSE
    // ---------------------------------------------------------

    private WorkJournalResponse convertToResponse(
            EmployeeWorkLog entry
    ) {

        return new WorkJournalResponse(
                entry.getId(),
                entry.getEmployee().getEmployeeId(),
                entry.getCompany().getCompanyId(),
                entry.getWorkDate(),
                entry.getWorkDescription(),
                entry.isHighlight(),
                entry.getHighlightCategory(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    // ---------------------------------------------------------
    // NORMALIZE HIGHLIGHT CATEGORY
    // ---------------------------------------------------------

    private String normalizeCategory(String category) {

        if (category == null || category.isBlank()) {
            return null;
        }

        return category.trim();
    }
}
