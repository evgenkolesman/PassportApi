package com.sperasoft.passportapi.configuration;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * All config was replaced to common config
 */

//@Configuration
@PropertySource("classpath:exceptions.properties")
@EnableConfigurationProperties
@RequiredArgsConstructor
@Deprecated
public class EnvConfig {

    private final Environment environment;


    public Environment getEnvironment(){
        return environment;
    }
}
