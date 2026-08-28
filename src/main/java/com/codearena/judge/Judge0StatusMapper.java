package com.codearena.judge;

import com.codearena.entity.Verdict;

/**
 * Judge0's numeric status IDs (see https://ce.judge0.com/#statuses-and-languages-status-get)
 * translated into our own Verdict enum. Kept as a standalone pure
 * function (no dependencies) specifically so it can be unit tested
 * without mocking any HTTP client.
 */
public final class Judge0StatusMapper {

    private Judge0StatusMapper() {
    }

    public static Verdict map(int judge0StatusId) {
        return switch (judge0StatusId) {
            case 3 -> Verdict.ACCEPTED;
            case 4 -> Verdict.WRONG_ANSWER;
            case 5 -> Verdict.TIME_LIMIT_EXCEEDED;
            case 6 -> Verdict.COMPILATION_ERROR;
            // 7-12: Runtime Error (SIGSEGV, SIGXFSZ, SIGFPE, SIGABRT, NZEC, Other)
            case 7, 8, 9, 10, 11, 12 -> Verdict.RUNTIME_ERROR;
            // 13: Internal Error (Judge0's own worker failed).
            // 14: Exec Format Error — a judge/environment misconfiguration rather
            // than something the user's code did wrong, so treated as our own
            // internal error rather than blamed on the submission.
            case 13, 14 -> Verdict.INTERNAL_ERROR;
            // 1 (In Queue) / 2 (Processing) should never reach this mapper — the
            // caller is expected to poll until Judge0SubmissionResult.isTerminal().
            // Any other/unknown id is treated conservatively as our own failure.
            default -> Verdict.INTERNAL_ERROR;
        };
    }
}
