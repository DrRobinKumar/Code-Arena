package com.codearena.entity;

/**
 * Used both as the per-test-case execution outcome (returned by
 * CodeExecutionService) and as the overall Submission verdict — the two
 * concepts are the same shape (PENDING/INTERNAL_ERROR only make sense at
 * the submission level, never returned by the judge itself).
 */
public enum Verdict {
    PENDING,
    ACCEPTED,
    WRONG_ANSWER,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    /** The judge itself failed to return a result (timeout, unreachable, malformed response). */
    INTERNAL_ERROR
}
