
package com.kaisha.payroll.auth.repository;

import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by mobile number
    Optional<User> findByMobileNumber(String mobileNumber);

    // Find user by email or mobile number
    Optional<User> findByEmailOrMobileNumber(
            String email,
            String mobileNumber
    );

    // Check existing username
    boolean existsByUsername(String username);

    // Check existing email
    boolean existsByEmail(String email);

    // Check existing mobile number
    boolean existsByMobileNumber(String mobileNumber);

    /*
     * Find user together with company.
     * Prevents LazyInitializationException
     * when accessing company information.
     */
    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.company
            WHERE u.username = :username
            """)
    Optional<User> findByUsernameWithCompany(
            @Param("username") String username
    );

    /*
     * Find ADMIN of a particular company.
     */
    Optional<User> findFirstByCompany_CompanyIdAndRole(
            String companyId,
            Role role
    );

    /*
     * EMPLOYEE → STAFF LOGIN CONNECTION
     *
     * Check whether an employee already has
     * a staff login account.
     */
    boolean existsByEmployee_Id(Long employeeId);

    /*
     * Find staff user linked to an employee.
     */
    Optional<User> findByEmployee_Id(Long employeeId);

}
