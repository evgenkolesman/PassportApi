package com.sperasoft.passportapi.model;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.configuration.CommonConfiguration;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.Data;

import java.time.Instant;

@Data
public class Passport {

    private String id;

    private String number;

    private Instant givenDate;

    private String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(PassportRequest passportRequest) {
        Passport passport = CommonConfiguration.configMapper().map(passportRequest, Passport.class);
        passport.setId(FriendlyId.createFriendlyId());
        return passport;
    }
}
