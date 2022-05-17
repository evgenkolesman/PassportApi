package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.model.Passport;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PassportResponse {

    private String id;

    private String number;

    private LocalDate givenDate;

    private String departmentCode;

    public static PassportResponse of(Passport passport) {
        return ModelMapperMaker.configMapper().map(passport, PassportResponse.class);
    }
}
