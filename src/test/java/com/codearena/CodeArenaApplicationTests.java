package com.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CodeArenaApplicationTests {

    @Test
    void contextLoads() {
        // Fails the build if any bean is misconfigured (wrong wiring,
        // missing dependency, bad property binding, etc.) — the cheapest
        // possible regression test for a Spring Boot app.
    }
}
