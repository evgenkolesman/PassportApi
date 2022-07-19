package com.sperasoft.passportapi;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Passport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Configuration
public class ConfigurationTest {
    private static final String HTTP_LOCALHOST = "http://localhost";

    @Bean
    public UriComponentsBuilder getUriComponentsBuilder() {
        return UriComponentsBuilder
                .fromHttpUrl(HTTP_LOCALHOST);
    }
}
