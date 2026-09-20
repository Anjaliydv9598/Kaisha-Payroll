package com.kaisha.payroll.auth.repository;

import com.kaisha.payroll.auth.entity.Role;
import com.kaisha.payroll.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByEmailOrMobileNumber(
            String email,
            String mobileNumber
    );

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);


    /*
     * Find user together with company.
     *
     * JOIN FETCH loads the Company in the same
     * database query.
     *
     * This prevents LazyInitializationException
     * when the company information is needed
     * after the repository method returns.
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
    Optional<User>
    findFirstByCompany_CompanyIdAndRole(
            String companyId,
            Role role
    );
}