package com.sperasoft.passportapi.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:exceptions.properties")
@EnableConfigurationProperties
@RequiredArgsConstructor
public class CommonConfiguration {
}
