package com.sperasoft.passportapi.model;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Passport {

    private String id;

    private String number;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate givenDate;

    private String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(PassportRequest passportRequest) {
        Passport passport = ModelMapperMaker.configMapper().map(passportRequest, Passport.class);
        passport.setId(FriendlyId.createFriendlyId());
        return passport;
    }
}
