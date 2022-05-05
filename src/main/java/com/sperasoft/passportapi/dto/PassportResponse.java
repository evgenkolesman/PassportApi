package com.sperasoft.passportapi.dto;

import com.sperasoft.passportapi.model.Passport;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Date;

@Data
public class PassportResponse {

    String id;

    String number;

    Date givenDate;

    String departmentCode;

    public static PassportResponse of(Passport passport) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model.map(passport, PassportResponse.class);
    }
}
