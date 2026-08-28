package com.codearena.judge;

import com.codearena.entity.Verdict;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class Judge0StatusMapperTest {

    @ParameterizedTest(name = "judge0 status {0} -> {1}")
    @CsvSource({
            "3, ACCEPTED",
            "4, WRONG_ANSWER",
            "5, TIME_LIMIT_EXCEEDED",
            "6, COMPILATION_ERROR",
            "7, RUNTIME_ERROR",
            "8, RUNTIME_ERROR",
            "9, RUNTIME_ERROR",
            "10, RUNTIME_ERROR",
            "11, RUNTIME_ERROR",
            "12, RUNTIME_ERROR",
            "13, INTERNAL_ERROR",
            "14, INTERNAL_ERROR",
            "999, INTERNAL_ERROR"
    })
    void map_shouldTranslateEveryKnownJudge0StatusId(int statusId, Verdict expected) {
        assertThat(Judge0StatusMapper.map(statusId)).isEqualTo(expected);
    }
}
