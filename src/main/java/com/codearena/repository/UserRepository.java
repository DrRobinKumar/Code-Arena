package com.codearena.repository;

import com.codearena.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Case-insensitive search across username/email/fullName. The
     * :search parameter is wrapped with '%' wildcards by the caller
     * (UserServiceImpl), keeping this query reusable for prefix/contains
     * searches alike. When search is null every clause short-circuits to
     * true, so the same query also serves as a plain "list all" query.
     */
    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL
                   OR LOWER(u.username) LIKE LOWER(:search)
                   OR LOWER(u.email) LIKE LOWER(:search)
                   OR LOWER(u.fullName) LIKE LOWER(:search))
            """)
    Page<User> search(@Param("search") String search, Pageable pageable);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Used by the JWT filter/auth flow to look a principal up by either
     * identifier, since we let users log in with username OR email.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}
