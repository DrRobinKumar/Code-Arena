package com.codearena.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class JudgeConfig {

    /**
     * A generous read timeout is deliberate: Judge0 compiling+running code
     * (especially JVM/compiled languages) can legitimately take several
     * seconds per batch. connectTimeout stays short since a hung TCP
     * handshake means the judge endpoint is simply unreachable.
     */
    @Bean
    public RestTemplate judge0RestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
