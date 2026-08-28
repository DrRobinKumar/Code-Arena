package com.codearena.entity;

/**
 * Judge0 language IDs are pinned to specific compiler/runtime versions
 * (see https://ce.judge0.com/languages). Kept here, not hardcoded at call
 * sites, so upgrading a runtime version later is a one-line change.
 */
public enum Language {
    C(50),              // C (GCC 9.2.0)
    CPP(54),             // C++ (GCC 9.2.0)
    JAVA(62),            // Java (OpenJDK 13.0.1)
    PYTHON(71),          // Python (3.8.1)
    JAVASCRIPT(63);      // JavaScript (Node.js 12.14.0)

    private final int judge0LanguageId;

    Language(int judge0LanguageId) {
        this.judge0LanguageId = judge0LanguageId;
    }

    public int getJudge0LanguageId() {
        return judge0LanguageId;
    }
}
