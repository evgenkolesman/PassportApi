package com.sperasoft.passportapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class TestConfiguration {
    private static final String HTTP_LOCALHOST = "http://localhost";

    @Bean
    public UriComponentsBuilder getUriComponentsBuilder() {
        return UriComponentsBuilder
                .fromHttpUrl(HTTP_LOCALHOST);
    }
}
