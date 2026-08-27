package com.codearena.entity;

/**
 * The fixed set of role names the application understands. Kept as an
 * enum (rather than free-text strings in the Role entity) so role
 * comparisons in code are compile-time safe, while the Role table itself
 * remains a real, queryable, seedable entity — not a hardcoded constant.
 */
public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN
}
