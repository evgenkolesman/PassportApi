package com.sperasoft.passportapi.configuration;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:exceptions.properties")
@EnableConfigurationProperties
@RequiredArgsConstructor
public class EnvConfig {

    private final Environment environment;

    @Bean
    public Environment getEnvironment(){
        return environment;
    }

    public String getProperty(String propName) {
        return getEnvironment().getProperty(propName);
    }
}
