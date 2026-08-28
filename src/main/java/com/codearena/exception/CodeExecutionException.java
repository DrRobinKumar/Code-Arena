package com.codearena.exception;

/**
 * Thrown when the execution engine itself (Judge0 today) fails to
 * produce a result — network failure, HTTP error, or exhausting the poll
 * budget without every submission reaching a terminal status. Distinct
 * from a Verdict.RUNTIME_ERROR, which means the judge worked fine and
 * the *user's code* failed at runtime.
 */
public class CodeExecutionException extends RuntimeException {

    public CodeExecutionException(String message) {
        super(message);
    }

    public CodeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
