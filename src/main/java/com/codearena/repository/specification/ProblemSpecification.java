package com.codearena.repository.specification;

import com.codearena.entity.DifficultyLevel;
import com.codearena.entity.Problem;
import com.codearena.entity.ProblemTag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Each filter is its own independent Specification, combined with `and()`
 * by the service layer. Adding a new filter later (e.g. "solved by me")
 * means adding one more static method here — nothing else changes
 * (Open/Closed principle).
 */
public final class ProblemSpecification {

    private ProblemSpecification() {
    }

    public static Specification<Problem> titleContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Problem> hasDifficulty(DifficultyLevel difficulty) {
        if (difficulty == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("difficulty"), difficulty);
    }

    /** Matches problems tagged with ANY of the given tag names (a union, not an intersection). */
    public static Specification<Problem> hasAnyTag(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        List<String> normalized = tagNames.stream().map(String::toLowerCase).toList();
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Problem, ProblemTag> problemTagJoin = root.join("problemTags", JoinType.INNER);
            return cb.lower(problemTagJoin.get("tag").get("name")).in(normalized);
        };
    }

    /**
     * Null-safe combinator: Specification.where(null) starts from an
     * always-true predicate, and each subsequent `.and(possiblyNull)` is a
     * no-op when the given specification is null (Spring Data handles this),
     * so callers don't need to hand-check which filters are active.
     */
    @SafeVarargs
    public static Specification<Problem> combine(Specification<Problem>... specs) {
        Specification<Problem> result = Specification.where(null);
        for (Specification<Problem> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
