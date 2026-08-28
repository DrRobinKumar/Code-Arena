package com.codearena.repository.specification;

import com.codearena.entity.Language;
import com.codearena.entity.Submission;
import com.codearena.entity.Verdict;
import org.springframework.data.jpa.domain.Specification;

/**
 * Mirrors ProblemSpecification's pattern: one independent Specification per
 * filter, combined by the service layer. Reused by both the owner-scoped
 * "my submissions" endpoint (always combined with byUser) and the
 * admin listing (byUser omitted, or applied when an admin filters by a
 * specific user).
 */
public final class SubmissionSpecification {

    private SubmissionSpecification() {
    }

    public static Specification<Submission> byUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Submission> byProblemId(Long problemId) {
        if (problemId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("problem").get("id"), problemId);
    }

    public static Specification<Submission> byVerdict(Verdict verdict) {
        if (verdict == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("verdict"), verdict);
    }

    public static Specification<Submission> byLanguage(Language language) {
        if (language == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("language"), language);
    }

    @SafeVarargs
    public static Specification<Submission> combine(Specification<Submission>... specs) {
        Specification<Submission> result = Specification.where(null);
        for (Specification<Submission> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
