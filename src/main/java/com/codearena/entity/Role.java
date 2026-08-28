package com.codearena.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A real, persisted role — not a hardcoded enum on User — so roles can be
 * looked up, listed, and later extended (e.g. a permissions join table)
 * without a schema migration on the User table itself. The finite set of
 * valid role names is still enum-backed via {@link RoleName} for
 * compile-time safety; only two rows (ROLE_USER, ROLE_ADMIN) are ever
 * expected to exist, seeded on startup by DataInitializer.
 */
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, unique = true)
    private RoleName name;

    @Column(length = 255)
    private String description;
}
