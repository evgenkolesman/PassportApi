package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;

@Data
@AllArgsConstructor
public class Passport {

    @NonNull
    private final String id;

    @NonNull
    private final String number;
    @NonNull
    private final Instant givenDate;
    @NonNull
    private final String departmentCode;

    private final boolean active;

    private final String description;

    public Passport(@NonNull String id,
                    @NonNull String number,
                    @NonNull Instant givenDate,
                    @NonNull String departmentCode) {
        this.id = id;
        this.number = number;
        this.givenDate = givenDate;
        this.departmentCode = departmentCode;
        this.active = true;
        this.description = "No description";
    }

    private Passport(String id, PassportRequest passportRequest) {
        this.id = id;
        this.number = passportRequest.getNumber();
        this.givenDate = passportRequest.getGivenDate();
        this.departmentCode = passportRequest.getDepartmentCode();
        this.active = true;
        this.description = "No description";
    }

    public static Passport of(final String id, final PassportRequest passportRequest) {
        return new Passport(id,
                passportRequest);
    }
}
