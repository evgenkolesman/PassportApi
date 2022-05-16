package com.sperasoft.passportapi;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperMaker {
    private static ModelMapper model;

    @Bean
    public ModelMapper getMapper() {
        model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model;
    }

    public static ModelMapper configMapper() {
        return model;
    }
}
