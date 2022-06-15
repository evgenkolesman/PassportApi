package com.sperasoft.passportapi.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.sperasoft.passportapi.model.Passport;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class PassportResponse {

    @NonNull
    private final String id;
    @NonNull
    private final String number;
    @NonNull
    private final Instant givenDate;
    @NonNull
    private final String departmentCode;

    public static PassportResponse of(final Passport passport) {
        return new PassportResponse(passport.getId(),
                passport.getNumber(),
                passport.getGivenDate(),
                passport.getDepartmentCode());
    }
}
