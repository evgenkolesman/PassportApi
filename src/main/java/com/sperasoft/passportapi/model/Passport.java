package com.sperasoft.passportapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
public class Passport {

    @NonNull
    private final String id;
    @NonNull
    private final String personId;
    @NonNull
    private final String number;
    @NonNull
    private final Instant givenDate;
    @NonNull
    private final String departmentCode;

    private final boolean active;

    private final String description;

    public Passport(@NonNull String id,
                    @NonNull String personId,
                    @NonNull String number,
                    @NonNull Instant givenDate,
                    @NonNull String departmentCode) {
        this.id = id;
        this.personId= personId;
        this.number = number;
        this.givenDate = givenDate.truncatedTo(ChronoUnit.MICROS);
        this.departmentCode = departmentCode;
        this.active = true;
        this.description = "No description";
    }
}
