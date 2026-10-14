package com.kaisha.payroll.attendance.repository;

import com.kaisha.payroll.attendance.entity.Attendance;
import com.kaisha.payroll.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance>
    findByCompany_CompanyIdOrderByAttendanceDateDesc(
            String companyId
    );

    List<Attendance>
    findByCompany_CompanyIdAndAttendanceDateOrderByEmployee_EmployeeIdAsc(
            String companyId,
            LocalDate attendanceDate
    );

    List<Attendance>
    findByCompany_CompanyIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            String companyId,
            LocalDate from,
            LocalDate to
    );

    List<Attendance>
    findByEmployee_EmployeeIdAndCompany_CompanyIdOrderByAttendanceDateDesc(
            String employeeId,
            String companyId
    );

    Optional<Attendance>
    findByIdAndCompany_CompanyId(
            Long id,
            String companyId
    );

    Optional<Attendance>
    findByEmployee_EmployeeIdAndCompany_CompanyIdAndAttendanceDate(
            String employeeId,
            String companyId,
            LocalDate attendanceDate
    );

    List<Attendance>
    findByCompany_CompanyIdAndAttendanceDateAndStatus(
            String companyId,
            LocalDate attendanceDate,
            AttendanceStatus status
    );
}