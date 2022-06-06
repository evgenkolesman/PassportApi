package com.sperasoft.passportapi.configuration;

import com.sperasoft.passportapi.model.Passport;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.List;
import java.util.function.BiPredicate;

@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:exceptions.properties")
@EnableConfigurationProperties
@RequiredArgsConstructor
public class CommonConfiguration {
    private static ModelMapper model;
    private final Environment environment;

    @Bean("EnvConfig")
    public Environment getEnvironment(){
        return environment;
    }

    @Bean
    public ModelMapper getMapper() {
        model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model;
    }

    public static ModelMapper configMapper() {
        return model;
    }

    @Bean
    public BiPredicate<Passport, List<Instant>> getPassportListBiPredicate() {
        return (passport, timeList) ->
                (timeList.get(0).isBefore(passport.getGivenDate())) &&
                        timeList.get(1).isAfter(passport.getGivenDate());
    }
}
