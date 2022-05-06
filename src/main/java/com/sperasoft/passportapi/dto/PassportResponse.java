package com.sperasoft.passportapi.dto;

import com.sperasoft.passportapi.model.Passport;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Date;

@Data
public class PassportResponse {

    private String id;

    private String number;

    private Date givenDate;

    private String departmentCode;

    public static PassportResponse of(Passport passport) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model.map(passport, PassportResponse.class);
    }
}
