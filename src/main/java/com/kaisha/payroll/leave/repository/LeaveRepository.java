
package com.kaisha.payroll.leave.repository;

import com.kaisha.payroll.leave.entity.Leave;
import com.kaisha.payroll.leave.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByCompanyIdOrderByCreatedAtDesc(String companyId);

    List<Leave> findByCompanyIdAndStatusOrderByCreatedAtDesc(
            String companyId,
            LeaveStatus status
    );

    List<Leave> findByCompanyIdAndEmployee_EmployeeIdOrderByCreatedAtDesc(
            String companyId,
            String employeeId
    );

    Optional<Leave> findByIdAndCompanyId(Long id, String companyId);

    List<Leave> findByCompanyIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String companyId,
            LeaveStatus status,
            LocalDate monthEnd,
            LocalDate monthStart
    );

    boolean existsByEmployee_IdAndCompanyIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            String companyId,
            List<LeaveStatus> statuses,
            LocalDate requestedEnd,
            LocalDate requestedStart
    );

    List<Leave> findByCompanyIdAndEmployee_EmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String companyId,
            String employeeId,
            LeaveStatus status,
            LocalDate monthEnd,
            LocalDate monthStart
    );

}
