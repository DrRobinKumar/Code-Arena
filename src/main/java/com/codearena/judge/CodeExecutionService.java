package com.codearena.judge;

import java.util.List;

/**
 * The execution-engine boundary. Everything above this interface
 * (SubmissionServiceImpl, controllers) is written against this contract
 * only. Swapping Judge0 for a self-hosted Docker sandbox later means
 * writing one new class implementing this interface and wiring it in
 * (e.g. via a config-driven @Primary bean) — nothing else in the
 * codebase changes.
 */
public interface CodeExecutionService {

    /**
     * Executes a single piece of code against one input. Used by "Run".
     * Default implementation delegates to executeBatch so a new engine
     * only has to implement one method; override this directly if a
     * given engine has a cheaper single-execution path.
     */
    default CodeExecutionResult execute(CodeExecutionRequest request) {
        return executeBatch(List.of(request)).get(0);
    }

    /**
     * Executes many requests (one per test case) as efficiently as the
     * underlying engine allows. Used by "Submit", where a problem may
     * have dozens of test cases — batching avoids one HTTP round trip
     * per test case against Judge0. Results are returned in the same
     * order as the input requests.
     */
    List<CodeExecutionResult> executeBatch(List<CodeExecutionRequest> requests);
}
