package com.sperasoft.passportapi.model;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.configuration.CommonConfiguration;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.Data;
import lombok.NonNull;

import java.time.Instant;

@Data
public class Passport {
    @NonNull
    private final String id;
    @NonNull
    private final String number;
    @NonNull
    private final Instant givenDate;
    @NonNull
    private final String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(final String id, final PassportRequest passportRequest) {
        return new Passport(id,
                passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                passportRequest.getDepartmentCode());
    }
}
