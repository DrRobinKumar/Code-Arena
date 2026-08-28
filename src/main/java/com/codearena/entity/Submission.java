package com.codearena.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A graded attempt at a Problem. Deliberately does NOT store a
 * per-test-case breakdown (see Phase 3 design notes) — only the
 * aggregate verdict/runtime/memory the platform needs to show a user
 * their submission history. Run Code (ad-hoc execution) is never
 * persisted at all; only Submit Code creates a Submission row.
 */
@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Lob
    @Column(nullable = false)
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private Verdict verdict = Verdict.PENDING;

    /** Worst (max) execution time across all test cases, in milliseconds. */
    private Long runtimeMs;

    /** Worst (max) memory usage across all test cases, in kilobytes. */
    private Long memoryKb;

    @Column(nullable = false)
    @Builder.Default
    private Integer testCasesPassed = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer testCasesTotal = 0;

    /** Populated only when verdict = COMPILATION_ERROR. */
    @Lob
    private String compileOutput;

    /** stderr / diagnostic detail from the first failing test case, for user feedback. */
    @Lob
    private String errorMessage;
}
