package com.codearena.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * A single input/expected-output pair used by the (future) judge engine.
 * hidden=false ("visible") test cases are safe to expose to end users, e.g.
 * for a "Run" feature against sample cases; hidden=true ones are used only
 * for grading on "Submit" and must never be returned by any user-facing
 * endpoint — that boundary is enforced in ProblemServiceImpl/ProblemMapper,
 * not here, since the entity itself has no notion of "who's asking".
 */
@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Lob
    @Column(nullable = false)
    private String input;

    @Lob
    @Column(nullable = false)
    private String expectedOutput;

    @Column(nullable = false)
    @Builder.Default
    private boolean hidden = true;
}
