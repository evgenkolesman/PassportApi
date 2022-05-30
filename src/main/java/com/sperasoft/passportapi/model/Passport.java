package com.sperasoft.passportapi.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Passport {

    private String id;

    private String number;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime givenDate;

    private String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(PassportRequest passportRequest) {
        Passport passport = ModelMapperMaker.configMapper().map(passportRequest, Passport.class);
        passport.setId(UUID.randomUUID().toString());
        return passport;
    }
}
