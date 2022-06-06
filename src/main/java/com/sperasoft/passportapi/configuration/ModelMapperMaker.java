package com.sperasoft.passportapi.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Configuration;

/**
 * All config was replaced to common config
 */

//@Configuration
@Deprecated
public class ModelMapperMaker {
    private static ModelMapper model;


    public ModelMapper getMapper() {
        model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model;
    }

    public static ModelMapper configMapper() {
        return model;
    }

}
