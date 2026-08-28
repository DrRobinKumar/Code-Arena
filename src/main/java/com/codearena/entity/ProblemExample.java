package com.codearena.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single worked example shown in the problem statement (distinct from
 * TestCase: examples are documentation the user reads, test cases are what
 * the future judge engine actually runs code against — even though a
 * "visible" TestCase often mirrors an example, they're conceptually
 * different and kept as separate structures).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemExample {

    @Lob
    @Column(nullable = false)
    private String input;

    @Lob
    @Column(nullable = false)
    private String output;

    @Lob
    private String explanation;
}
