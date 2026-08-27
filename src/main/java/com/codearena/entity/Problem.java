package com.codearena.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root. TestCase and ProblemTag are owned entirely by Problem —
 * cascade ALL + orphanRemoval means adding/removing them is just mutating
 * these collections and saving the Problem; there is no separate
 * repository/controller for either. Examples and hints are simple,
 * always-problem-owned data modeled as element collections rather than
 * their own entities.
 */
@Entity
@Table(name = "problems", uniqueConstraints = @UniqueConstraint(name = "uk_problems_slug", columnNames = "slug"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 220)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DifficultyLevel difficulty;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    private String constraints;

    @Lob
    private String inputFormat;

    @Lob
    private String outputFormat;

    /** Only ever returned to admins — see ProblemMapper/ProblemServiceImpl. */
    @Lob
    private String editorial;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeLimitMs = 2000;

    @Column(nullable = false)
    @Builder.Default
    private Integer memoryLimitKb = 262144;

    @ElementCollection
    @CollectionTable(name = "problem_examples", joinColumns = @JoinColumn(name = "problem_id"))
    @OrderColumn(name = "example_order")
    @Builder.Default
    private List<ProblemExample> examples = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "problem_hints", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "hint", length = 2000)
    @OrderColumn(name = "hint_order")
    @Builder.Default
    private List<String> hints = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    @Builder.Default
    private List<ProblemTag> problemTags = new ArrayList<>();

    // --- Aggregate-consistency helpers: keep both sides of each
    // bidirectional relationship in sync, and centralize the
    // clear-then-repopulate pattern used on full-replace updates.

    public void replaceTestCases(List<TestCase> newTestCases) {
        this.testCases.clear();
        newTestCases.forEach(tc -> tc.setProblem(this));
        this.testCases.addAll(newTestCases);
    }

    public void replaceProblemTags(List<ProblemTag> newProblemTags) {
        this.problemTags.clear();
        newProblemTags.forEach(pt -> pt.setProblem(this));
        this.problemTags.addAll(newProblemTags);
    }
}
