package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.model.Passport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;

@Data
@AllArgsConstructor
public class PassportResponse {

    @NonNull
    private final String id;
    @NonNull
    private final String number;
    @NonNull
    private final Instant givenDate;
    @NonNull
    private final String departmentCode;

    private PassportResponse(Passport passport) {
        this.id = passport.getId();
        this.number = passport.getNumber();
        this.givenDate = passport.getGivenDate();
        this.departmentCode = passport.getDepartmentCode();
    }

    public static PassportResponse of(final Passport passport) {
        return new PassportResponse(passport);
    }
}
