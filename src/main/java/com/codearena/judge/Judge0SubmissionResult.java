package com.codearena.judge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Maps 1:1 onto a single element of Judge0's response to GET
 * /submissions/batch?tokens=... (or the immediate response body when
 * wait=true is honored). All text fields arrive base64-encoded since we
 * always request base64_encoded=true.
 */
@Getter
@Setter
public class Judge0SubmissionResult {

    private String token;
    private String stdout;
    private String stderr;

    @JsonProperty("compile_output")
    private String compileOutput;

    private String message;

    /** Seconds, as a decimal string (e.g. "0.014"); null while still queued/processing. */
    private String time;

    /** Kilobytes; null on compile error or while still queued/processing. */
    private Integer memory;

    private Status status;

    @Getter
    @Setter
    public static class Status {
        private int id;
        private String description;
    }

    /** Judge0 status IDs 1 (In Queue) and 2 (Processing) mean the result isn't ready yet. */
    public boolean isTerminal() {
        return status != null && status.getId() > 2;
    }

    public String decodedStdout() {
        return decode(stdout);
    }

    public String decodedStderr() {
        return decode(stderr);
    }

    public String decodedCompileOutput() {
        return decode(compileOutput);
    }

    public Long timeInMillis() {
        if (time == null) {
            return null;
        }
        return Math.round(Double.parseDouble(time) * 1000);
    }

    private static String decode(String base64) {
        if (base64 == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }
}
