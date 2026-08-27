package com.codearena.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    /** BCrypt hash — never store or return plaintext passwords. */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    /**
     * EAGER on purpose: we need the role (and its name) on virtually every
     * read of a User — auth, /me, admin listings — so lazy-loading it would
     * just mean an extra round trip almost every time anyway. For a
     * single-valued, rarely-changing reference like this, EAGER is the
     * pragmatic choice; LAZY is reserved for collections and heavier
     * associations (e.g. a user's submissions).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_users_role"))
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
