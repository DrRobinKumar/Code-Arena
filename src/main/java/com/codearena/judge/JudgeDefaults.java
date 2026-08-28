package com.codearena.judge;

/**
 * Defaults used when a Run request isn't tied to a specific problem (and
 * therefore has no problem.timeLimitMs/memoryLimitKb to inherit).
 * Intentionally duplicated from Problem's own @Builder.Default values
 * rather than shared via a constant — the judge package must not depend
 * on the entity package's internals, and two small integers are cheap
 * to keep in sync manually versus introducing that dependency direction.
 */
public final class JudgeDefaults {

    public static final int DEFAULT_TIME_LIMIT_MS = 2000;
    public static final int DEFAULT_MEMORY_LIMIT_KB = 262144;

    private JudgeDefaults() {
    }
}
