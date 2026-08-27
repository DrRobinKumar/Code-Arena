package com.codearena.entity;

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

/**
 * Explicit join entity (rather than an implicit @ManyToMany join table) so
 * the Problem<->Tag association has its own identity and room to grow
 * (e.g. addedBy/addedAt) without a schema migration later. Owned entirely
 * by Problem (see Problem.problemTags — cascade ALL + orphanRemoval), so
 * there is no independent repository or controller for this entity.
 */
@Entity
@Table(name = "problem_tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_problem_tags_problem_tag", columnNames = {"problem_id", "tag_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
