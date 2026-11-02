package com.kaisha.payroll.attendance.service;

import com.kaisha.payroll.attendance.dto.AttendanceRequest;
import com.kaisha.payroll.attendance.dto.AttendanceResponse;
import com.kaisha.payroll.attendance.entity.Attendance;
import com.kaisha.payroll.attendance.entity.AttendanceStatus;
import com.kaisha.payroll.attendance.repository.AttendanceRepository;
import com.kaisha.payroll.auth.repository.UserRepository;
import com.kaisha.payroll.auth.entity.User;
import com.kaisha.payroll.company.entity.Company;
import com.kaisha.payroll.employee.entity.Employee;
import com.kaisha.payroll.employee.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AttendanceDayRuleService attendanceDayRuleService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            AttendanceDayRuleService attendanceDayRuleService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.attendanceDayRuleService = attendanceDayRuleService;
    }
    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------

    public List<AttendanceResponse> getAllAttendance(String email) {

        Company company = getCompany(email);

        return attendanceRepository
                .findByCompany_CompanyIdOrderByAttendanceDateDesc(
                        company.getCompanyId()
                )
                .stream()
                .map(AttendanceResponse::new)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    public AttendanceResponse getAttendance(
            Long id,
            String email
    ) {

        Company company = getCompany(email);

        Attendance attendance =
                attendanceRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                company.getCompanyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance record not found"
                                )
                        );

        return new AttendanceResponse(attendance);
    }

    // ---------------------------------------------------------
    // GET EMPLOYEE ATTENDANCE
    // ---------------------------------------------------------

    public List<AttendanceResponse> getEmployeeAttendance(
            String employeeId,
            String email
    ) {

        Company company = getCompany(email);

        return attendanceRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyIdOrderByAttendanceDateDesc(
                        employeeId,
                        company.getCompanyId()
                )
                .stream()
                .map(AttendanceResponse::new)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY DATE
    // ---------------------------------------------------------

    public List<AttendanceResponse> getByDate(
            LocalDate date,
            String email
    ) {

        Company company = getCompany(email);

        return attendanceRepository
                .findByCompany_CompanyIdAndAttendanceDateOrderByEmployee_EmployeeIdAsc(
                        company.getCompanyId(),
                        date
                )
                .stream()
                .map(AttendanceResponse::new)
                .toList();
    }

    // ---------------------------------------------------------
    // GET DATE RANGE
    // ---------------------------------------------------------

    public List<AttendanceResponse> getByDateRange(
            LocalDate from,
            LocalDate to,
            String email
    ) {

        if (from == null || to == null) {
            throw new RuntimeException(
                    "From date and to date are required"
            );
        }

        if (from.isAfter(to)) {
            throw new RuntimeException(
                    "From date cannot be after to date"
            );
        }

        Company company = getCompany(email);

        return attendanceRepository
                .findByCompany_CompanyIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
                        company.getCompanyId(),
                        from,
                        to
                )
                .stream()
                .map(AttendanceResponse::new)
                .toList();
    }

    // ---------------------------------------------------------
    // CREATE ATTENDANCE
    // ---------------------------------------------------------

    public AttendanceResponse createAttendance(
            AttendanceRequest request,
            String email
    ) {

        validateRequest(request);

        Company company = getCompany(email);

        Employee employee =
                getEmployee(
                        request.getEmployeeId(),
                        company.getCompanyId()
                );

        LocalDate date = request.getAttendanceDate();

        if (attendanceRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyIdAndAttendanceDate(
                        employee.getEmployeeId(),
                        company.getCompanyId(),
                        date
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Attendance already exists for employee "
                            + employee.getEmployeeId()
                            + " on "
                            + date
            );
        }

        Attendance attendance = new Attendance();

        attendance.setEmployee(employee);
        attendance.setCompany(company);
        attendance.setAttendanceDate(date);

        attendance.setPunchIn(request.getPunchIn());
        attendance.setPunchOut(request.getPunchOut());

        attendance.setStatus(
                determineStatus(
                        employee,
                        request.getAttendanceDate(),
                        request.getPunchIn(),
                        request.getPunchOut(),
                        request.getStatus()
                )
        );

        attendance.setCorrectionReason(
                clean(request.getCorrectionReason())
        );

        attendance.setModifiedBy(email);
        attendance.setModifiedAt(LocalDateTime.now());

        calculateWorkingHours(attendance);

        Attendance saved =
                attendanceRepository.save(attendance);

        return new AttendanceResponse(saved);
    }

    // ---------------------------------------------------------
    // UPDATE ATTENDANCE
    // ---------------------------------------------------------

    public AttendanceResponse updateAttendance(
            Long id,
            AttendanceRequest request,
            String email
    ) {

        validateRequest(request);

        Company company = getCompany(email);

        Attendance attendance =
                attendanceRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                company.getCompanyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance record not found"
                                )
                        );

        Employee employee =
                getEmployee(
                        request.getEmployeeId(),
                        company.getCompanyId()
                );

        /*
         * If employee/date is changed, make sure another
         * attendance record doesn't already exist.
         */
        attendanceRepository
                .findByEmployee_EmployeeIdAndCompany_CompanyIdAndAttendanceDate(
                        employee.getEmployeeId(),
                        company.getCompanyId(),
                        request.getAttendanceDate()
                )
                .ifPresent(existing -> {

                    if (!existing.getId().equals(id)) {

                        throw new RuntimeException(
                                "Attendance already exists for employee "
                                        + employee.getEmployeeId()
                                        + " on "
                                        + request.getAttendanceDate()
                        );
                    }
                });

        attendance.setEmployee(employee);
        attendance.setAttendanceDate(
                request.getAttendanceDate()
        );

        attendance.setPunchIn(
                request.getPunchIn()
        );

        attendance.setPunchOut(
                request.getPunchOut()
        );

        attendance.setStatus(
                determineStatus(
                        employee,
                        request.getAttendanceDate(),
                        request.getPunchIn(),
                        request.getPunchOut(),
                        request.getStatus()
                )
        );

        attendance.setCorrectionReason(
                clean(request.getCorrectionReason())
        );

        attendance.setModifiedBy(email);
        attendance.setModifiedAt(LocalDateTime.now());

        calculateWorkingHours(attendance);

        return new AttendanceResponse(
                attendanceRepository.save(attendance)
        );
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    public void deleteAttendance(
            Long id,
            String email
    ) {

        Company company = getCompany(email);

        Attendance attendance =
                attendanceRepository
                        .findByIdAndCompany_CompanyId(
                                id,
                                company.getCompanyId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Attendance record not found"
                                )
                        );

        attendanceRepository.delete(attendance);
    }

    // ---------------------------------------------------------
    // PUNCH IN
    // ---------------------------------------------------------

    public AttendanceResponse punchIn(
            String employeeId,
            String email
    ) {

        Company company = getCompany(email);

        Employee employee =
                getEmployee(
                        employeeId,
                        company.getCompanyId()
                );

        LocalDate today = LocalDate.now();

        Attendance attendance =
                attendanceRepository
                        .findByEmployee_EmployeeIdAndCompany_CompanyIdAndAttendanceDate(
                                employeeId,
                                company.getCompanyId(),
                                today
                        )
                        .orElse(null);

        if (attendance == null) {

            attendance = new Attendance();

            attendance.setEmployee(employee);
            attendance.setCompany(company);
            attendance.setAttendanceDate(today);

        } else if (attendance.getPunchIn() != null) {

            throw new RuntimeException(
                    "Employee has already punched in today"
            );
        }

        attendance.setPunchIn(LocalTime.now());

        attendance.setStatus(
                determineStatus(
                        employee,
                        today,
                        attendance.getPunchIn(),
                        attendance.getPunchOut(),
                        null
                )
        );

        attendance.setModifiedBy(email);
        attendance.setModifiedAt(LocalDateTime.now());

        calculateWorkingHours(attendance);

        return new AttendanceResponse(
                attendanceRepository.save(attendance)
        );
    }

    // ---------------------------------------------------------
    // PUNCH OUT
    // ---------------------------------------------------------

    public AttendanceResponse punchOut(
            String employeeId,
            String email
    ) {

        Company company = getCompany(email);

        Attendance attendance =
                attendanceRepository
                        .findByEmployee_EmployeeIdAndCompany_CompanyIdAndAttendanceDate(
                                employeeId,
                                company.getCompanyId(),
                                LocalDate.now()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee has not punched in today"
                                )
                        );

        if (attendance.getPunchIn() == null) {

            throw new RuntimeException(
                    "Employee has not punched in today"
            );
        }

        if (attendance.getPunchOut() != null) {

            throw new RuntimeException(
                    "Employee has already punched out today"
            );
        }

        attendance.setPunchOut(LocalTime.now());

        attendance.setStatus(
                determineStatus(
                        attendance.getEmployee(),
                        attendance.getAttendanceDate(),
                        attendance.getPunchIn(),
                        attendance.getPunchOut(),
                        null
                )
        );

        attendance.setModifiedBy(email);
        attendance.setModifiedAt(LocalDateTime.now());

        calculateWorkingHours(attendance);

        return new AttendanceResponse(
                attendanceRepository.save(attendance)
        );
    }

    // ---------------------------------------------------------
    // MISSING PUNCH -> ABSENT AT 11 PM
    // ---------------------------------------------------------

    // ---------------------------------------------------------
// MISSING PUNCH REVIEW AT 11 PM
// Do not automatically mark records ABSENT.
// Keep them pending for admin review.
// ---------------------------------------------------------

    public int markMissingPunchesAbsent() {

        LocalDate today = LocalDate.now();

        List<Attendance> records =
                attendanceRepository.findAll()
                        .stream()
                        .filter(attendance ->
                                today.equals(attendance.getAttendanceDate())
                                        && attendance.getStatus()
                                        == AttendanceStatus.MISSING_PUNCH
                        )
                        .toList();

        // Records remain MISSING_PUNCH until admin review.
        return records.size();
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private Company getCompany(String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Logged-in user not found"
                                )
                        );

        if (user.getCompany() == null) {

            throw new RuntimeException(
                    "User is not associated with a company"
            );
        }

        return user.getCompany();
    }

    private Employee getEmployee(String employeeId, String companyId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required");
        }

        Employee employee = employeeRepository
                .findByEmployeeIdAndCompany_CompanyId(employeeId, companyId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        if (!employee.isActive()) {
            throw new RuntimeException("Inactive employees cannot record attendance");
        }

        if (!employee.isDetailsCompleted()) {
            throw new RuntimeException(
                    "Complete employee details before recording attendance");
        }

        return employee;
    }


    private void validateRequest(
            AttendanceRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Attendance request is required"
            );
        }

        if (request.getEmployeeId() == null ||
                request.getEmployeeId().isBlank()) {

            throw new RuntimeException(
                    "Employee ID is required"
            );
        }

        if (request.getAttendanceDate() == null) {

            throw new RuntimeException(
                    "Attendance date is required"
            );
        }

        if (request.getPunchIn() != null &&
                request.getPunchOut() != null &&
                request.getPunchOut()
                        .isBefore(request.getPunchIn())) {

            throw new RuntimeException(
                    "Punch out cannot be before punch in"
            );
        }
    }

    private AttendanceStatus determineStatus(
            Employee employee,
            LocalDate date,
            LocalTime punchIn,
            LocalTime punchOut,
            AttendanceStatus requestedStatus
    ) {

        // Preserve explicitly selected admin statuses.
        if (requestedStatus == AttendanceStatus.LEAVE
                || requestedStatus == AttendanceStatus.HOLIDAY
                || requestedStatus == AttendanceStatus.WEEK_OFF
                || requestedStatus == AttendanceStatus.HALF_DAY
                || requestedStatus == AttendanceStatus.ABSENT) {

            return requestedStatus;
        }

        // Actual punches take priority over automatic day classification.
        if (punchIn != null && punchOut != null) {
            return AttendanceStatus.PRESENT;
        }

        if (punchIn != null) {
            return AttendanceStatus.MISSING_PUNCH;
        }

        // Check company holidays and department weekly offs.
        AttendanceStatus nonWorkingStatus =
                attendanceDayRuleService.getNonWorkingDayStatus(
                        employee,
                        date
                );

        if (nonWorkingStatus != null) {
            return nonWorkingStatus;
        }

        return AttendanceStatus.ABSENT;
    }

    private void calculateWorkingHours(
            Attendance attendance
    ) {

        if (attendance.getPunchIn() == null ||
                attendance.getPunchOut() == null) {

            attendance.setWorkingHours(null);
            return;
        }

        Duration duration =
                Duration.between(
                        attendance.getPunchIn(),
                        attendance.getPunchOut()
                );

        double hours =
                duration.toMinutes() / 60.0;

        attendance.setWorkingHours(
                BigDecimal.valueOf(hours)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}