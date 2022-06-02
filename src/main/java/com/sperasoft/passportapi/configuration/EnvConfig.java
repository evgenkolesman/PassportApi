package com.sperasoft.passportapi.configuration;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:exceptions.properties")
@EnableConfigurationProperties
@RequiredArgsConstructor
public class EnvConfig {

    private final Environment environment;

    @Bean(value = "EnvConfig")
    public Environment getEnvironment(){
        return environment;
    }
}
