package com.sperasoft.passportapi.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.model.Passport;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.time.LocalDate;

@Data
public class PassportResponse {

    private String id;

    private String number;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate givenDate;

    private String departmentCode;

    public static PassportResponse of(Passport passport) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return model.map(passport, PassportResponse.class);
    }
}
