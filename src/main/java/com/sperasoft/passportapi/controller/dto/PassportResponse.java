package com.sperasoft.passportapi.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.model.Passport;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class PassportResponse {

    private String id;

    private String number;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant givenDate;

    private String departmentCode;

    public static PassportResponse of(Passport passport) {
        return ModelMapperMaker.configMapper().map(passport, PassportResponse.class);
    }
}
