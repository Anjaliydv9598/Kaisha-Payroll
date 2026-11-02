package com.kaisha.payroll.attendance.service;

import com.kaisha.payroll.attendance.entity.AttendanceStatus;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.calendar.entity.CalendarEvent;
import com.kaisha.payroll.calendar.entity.CalendarEventType;
import com.kaisha.payroll.calendar.repository.CalendarEventRepository;
import com.kaisha.payroll.department.entity.Department;
import com.kaisha.payroll.department.entity.DepartmentWeeklyOff;
import com.kaisha.payroll.department.repository.DepartmentRepository;
import com.kaisha.payroll.department.repository.DepartmentWeeklyOffRepository;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.entity.EmployeeField;
import com.kaisha.payroll.employee.repository.EmployeeFieldRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceDayRuleService {

    private final EmployeeFieldRepository employeeFieldRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentWeeklyOffRepository weeklyOffRepository;
    private final CalendarEventRepository calendarEventRepository;

    public AttendanceDayRuleService(
            EmployeeFieldRepository employeeFieldRepository,
            DepartmentRepository departmentRepository,
            DepartmentWeeklyOffRepository weeklyOffRepository,
            CalendarEventRepository calendarEventRepository
    ) {
        this.employeeFieldRepository = employeeFieldRepository;
        this.departmentRepository = departmentRepository;
        this.weeklyOffRepository = weeklyOffRepository;
        this.calendarEventRepository = calendarEventRepository;
    }

    /**
     * Returns HOLIDAY or WEEK_OFF when the employee
     * is not scheduled to work on this date.
     *
     * Returns null when the employee is scheduled to work.
     */
    @Transactional(readOnly = true)
    public AttendanceStatus getNonWorkingDayStatus(
            Employee employee,
            LocalDate date
    ) {

        if (employee == null || date == null) {
            throw new RuntimeException(
                    "Employee and attendance date are required."
            );
        }

        String companyId = employee.getCompany().getCompanyId();

        // 1. Read the company's calendar events for this date.
        List<CalendarEvent> events =
                calendarEventRepository
                        .findByCompany_CompanyIdAndEventDateBetweenOrderByEventDateAsc(
                                companyId,
                                date,
                                date
                        );

        // 2. A WORKING_DAY override takes priority.
        boolean workingDayOverride = events.stream()
                .anyMatch(event ->
                        event.getEventType() == CalendarEventType.WORKING_DAY
                );

        if (workingDayOverride) {
            return null;
        }

        // 3. Company-wide public holiday.
        boolean holiday = events.stream()
                .anyMatch(event ->
                        event.getEventType() == CalendarEventType.HOLIDAY
                );

        if (holiday) {
            return AttendanceStatus.HOLIDAY;
        }

        // 4. Find the employee's department from EmployeeField.
        List<EmployeeField> fields =
                employeeFieldRepository
                        .findByEmployee_IdOrderByFieldIdAsc(
                                employee.getId()
                        );

        Optional<String> departmentName = fields.stream()
                .filter(field ->
                        field.getFieldName() != null
                                && field.getFieldName()
                                .equalsIgnoreCase("Department")
                )
                .map(EmployeeField::getFieldValue)
                .filter(value ->
                        value != null && !value.isBlank()
                )
                .findFirst();

        if (departmentName.isEmpty()) {
            throw new RuntimeException(
                    "Department is not configured for employee "
                            + employee.getEmployeeId()
            );
        }

        // 5. Resolve the active department within this company.
        String cleanDepartmentName =
                departmentName.get().trim();

        Department department =
                departmentRepository
                        .findByCompany_CompanyIdAndActiveTrueOrderByDepartmentNameAsc(
                                companyId
                        )
                        .stream()
                        .filter(item ->
                                item.getDepartmentName()
                                        .equalsIgnoreCase(cleanDepartmentName)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Department not found: "
                                                + cleanDepartmentName
                                )
                        );

        // 6. Get the department's configured weekly offs.
        List<DepartmentWeeklyOff> weeklyOffs =
                weeklyOffRepository
                        .findByDepartment_DepartmentIdOrderByDayOfWeekAsc(
                                department.getDepartmentId()
                        );

        // Default configuration: Saturday and Sunday.
        boolean isWeeklyOff;

        if (weeklyOffs.isEmpty()) {
            isWeeklyOff =
                    date.getDayOfWeek() == DayOfWeek.SATURDAY
                            || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        } else {
            isWeeklyOff = weeklyOffs.stream()
                    .anyMatch(weeklyOff ->
                            weeklyOff.getDayOfWeek() == date.getDayOfWeek()
                    );
        }

        return isWeeklyOff
                ? AttendanceStatus.WEEK_OFF
                : null;
    }
}