
package com.kaisha.payroll.leave.service;

import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import com.kaisha.payroll.leave.dto.LeaveDecisionRequest;
import com.kaisha.payroll.leave.dto.LeaveRequest;
import com.kaisha.payroll.leave.dto.LeaveResponse;
import com.kaisha.payroll.leave.entity.Leave;
import com.kaisha.payroll.leave.entity.LeaveStatus;
import com.kaisha.payroll.leave.repository.LeaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    public LeaveService(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository,
            CurrentUserService currentUserService
    ) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public LeaveResponse applyLeave(LeaveRequest request) {

        String companyId = currentUserService.getCurrentCompanyId();

        if (request == null ||
                request.employeeId() == null ||
                request.employeeId().isBlank() ||
                request.startDate() == null ||
                request.endDate() == null ||
                request.leaveType() == null) {
            throw new IllegalArgumentException("Required leave details are missing.");
        }

        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("Leave reason is required.");
        }

        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(
                        request.employeeId(), companyId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Active employee not found in your company.")
                );

        boolean overlapping = leaveRepository
                .existsByEmployee_IdAndCompanyIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee.getId(),
                        companyId,
                        List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                        request.endDate(),
                        request.startDate()
                );

        if (overlapping) {
            throw new IllegalArgumentException(
                    "A pending or approved leave already overlaps these dates."
            );
        }

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setCompanyId(companyId);
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setLeaveType(request.leaveType());
        leave.setReason(request.reason().trim());
        leave.setStatus(LeaveStatus.PENDING);

        return toResponse(leaveRepository.save(leave));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {

        String companyId = currentUserService.getCurrentCompanyId();

        return leaveRepository
                .findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {

        String companyId = currentUserService.getCurrentCompanyId();

        return leaveRepository
                .findByCompanyIdAndStatusOrderByCreatedAtDesc(
                        companyId, LeaveStatus.PENDING
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getEmployeeLeaves(String employeeId) {

        String companyId = currentUserService.getCurrentCompanyId();

        employeeRepository
                .findByEmployeeIdAndCompany_CompanyIdAndActiveTrue(employeeId, companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Active employee not found.")
                );

        return leaveRepository
                .findByCompanyIdAndEmployee_EmployeeIdOrderByCreatedAtDesc(
                        companyId, employeeId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LeaveResponse decideLeave(
            Long leaveId,
            LeaveDecisionRequest request
    ) {

        if (request == null ||
                (request.status() != LeaveStatus.APPROVED &&
                        request.status() != LeaveStatus.REJECTED)) {
            throw new IllegalArgumentException(
                    "Decision must be APPROVED or REJECTED."
            );
        }

        String companyId = currentUserService.getCurrentCompanyId();

        Leave leave = leaveRepository.findByIdAndCompanyId(leaveId, companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Leave request not found.")
                );

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending leave requests can be approved or rejected."
            );
        }

        leave.setStatus(request.status());
        leave.setAdminRemark(request.adminRemark());
        leave.setDecidedAt(LocalDateTime.now());

        return toResponse(leaveRepository.save(leave));
    }

    private LeaveResponse toResponse(Leave leave) {

        return new LeaveResponse(
                leave.getId(),
                leave.getEmployee().getEmployeeId(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getLeaveType(),
                leave.getStatus(),
                leave.getReason(),
                leave.getAdminRemark(),
                leave.getCreatedAt(),
                leave.getDecidedAt()
        );
    }
}
