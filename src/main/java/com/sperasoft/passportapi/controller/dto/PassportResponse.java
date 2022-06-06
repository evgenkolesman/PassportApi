package com.sperasoft.passportapi.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.sperasoft.passportapi.configuration.CommonConfiguration;
import com.sperasoft.passportapi.model.Passport;
import lombok.Data;

import java.time.Instant;

@Data
public class PassportResponse {

    private String id;

    private String number;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant givenDate;

    private String departmentCode;

    public static PassportResponse of(Passport passport) {
        return CommonConfiguration.configMapper().map(passport, PassportResponse.class);
    }
}
