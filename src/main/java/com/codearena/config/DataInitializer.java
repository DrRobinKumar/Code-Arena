package com.codearena.config;

import com.codearena.entity.Role;
import com.codearena.entity.RoleName;
import com.codearena.entity.User;
import com.codearena.repository.RoleRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs once on application startup to guarantee two invariants the rest of
 * the app depends on:
 *   1. Both roles (ROLE_USER, ROLE_ADMIN) exist as rows before any
 *      registration or admin-check happens — registration would otherwise
 *      fail on a fresh database with "role not found".
 *   2. At least one admin account exists, so there's a way into the
 *      admin-only endpoints without manually editing the database.
 * Both operations are idempotent (existsBy checks) so this is safe to run
 * on every restart, including against an already-seeded database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap-admin.email:admin@codearena.local}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:Admin@12345}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRole(RoleName.ROLE_USER, "Standard platform user");
        Role adminRole = seedRole(RoleName.ROLE_ADMIN, "Full administrative access");
        seedBootstrapAdmin(adminRole);
    }

    private Role seedRole(RoleName name, String description) {
        return roleRepository.findByName(name).orElseGet(() -> {
            log.info("Seeding missing role: {}", name);
            return roleRepository.save(Role.builder().name(name).description(description).build());
        });
    }

    private void seedBootstrapAdmin(Role adminRole) {
        boolean adminExists = userRepository.existsByUsername(adminUsername);
        if (adminExists) {
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .fullName("CodeArena Administrator")
                .role(adminRole)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.warn("Bootstrap admin account created (username='{}'). " +
                "Change its password immediately in a real environment via app.bootstrap-admin.password.", adminUsername);
    }
}
