
package com.kaisha.payroll.department.service;

import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.service.CurrentUserService;
import com.kaisha.payroll.department.dto.WeeklyOffRequest;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.entity.DepartmentWeeklyOff;
import com.kaisha.payroll.department.repository.DepartmentRepository;
import com.kaisha.payroll.department.repository.DepartmentWeeklyOffRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepartmentWeeklyOffService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentWeeklyOffRepository weeklyOffRepository;
    private final CurrentUserService currentUserService;

    public DepartmentWeeklyOffService(
            DepartmentRepository departmentRepository,
            DepartmentWeeklyOffRepository weeklyOffRepository,
            CurrentUserService currentUserService) {

        this.departmentRepository = departmentRepository;
        this.weeklyOffRepository = weeklyOffRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Get weekly off days for a department.
     * Default: Saturday and Sunday.
     */
    @Transactional(readOnly = true)
    public List<DayOfWeek> getWeeklyOffs(Long departmentId) {

        String companyId = currentUserService.getCurrentCompanyId();

        Department department = getDepartment(departmentId, companyId);

        List<DepartmentWeeklyOff> savedDays =
                weeklyOffRepository
                        .findByDepartment_DepartmentIdOrderByDayOfWeekAsc(
                                department.getDepartmentId()
                        );

        // Return default weekly offs if no configuration exists.
        if (savedDays.isEmpty()) {
            return List.of(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
            );
        }

        return savedDays.stream()
                .map(DepartmentWeeklyOff::getDayOfWeek)
                .toList();
    }

    /**
     * Update weekly off days.
     * Only ADMIN users can update settings.
     */
    @Transactional
    public List<DayOfWeek> updateWeeklyOffs(
            Long departmentId,
            WeeklyOffRequest request) {

        requireAdmin();

        if (request == null || request.getDays() == null) {
            throw new IllegalArgumentException(
                    "Weekly off days are required."
            );
        }

        List<DayOfWeek> days = request.getDays();

        // Validate null values.
        if (days.stream().anyMatch(day -> day == null)) {
            throw new IllegalArgumentException(
                    "Weekly off days cannot contain null values."
            );
        }

        // Validate duplicate values.
        if (days.size() != days.stream().distinct().count()) {
            throw new IllegalArgumentException(
                    "Duplicate weekly off days are not allowed."
            );
        }

        // Validate maximum number of days.
        if (days.size() > 7) {
            throw new IllegalArgumentException(
                    "Maximum seven weekly off days are allowed."
            );
        }

        String companyId = currentUserService.getCurrentCompanyId();

        Department department = getDepartment(departmentId, companyId);

        // Remove previous configuration.
        weeklyOffRepository.deleteByDepartment_DepartmentId(
                department.getDepartmentId()
        );

        // Create new configuration.
        List<DepartmentWeeklyOff> records = new ArrayList<>();

        for (DayOfWeek day : days) {
            records.add(
                    new DepartmentWeeklyOff(department, day)
            );
        }

        // Save updated configuration.
        weeklyOffRepository.saveAll(records);

        return days;
    }

    /**
     * Find an active department belonging to the current company.
     */
    private Department getDepartment(
            Long departmentId,
            String companyId) {

        return departmentRepository
                .findByDepartmentIdAndCompany_CompanyIdAndActiveTrue(
                        departmentId,
                        companyId
                )
                .orElseThrow(() -> new RuntimeException(
                        "Department not found or access denied."
                ));
    }

    /**
     * Verify that the logged-in user is an ADMIN.
     */
    private void requireAdmin() {

        var user = currentUserService.getCurrentUser();

        if (user == null || user.getRole() != Role.ADMIN) {
            throw new RuntimeException(
                    "Only administrators can update weekly off settings."
            );
        }
    }
}
