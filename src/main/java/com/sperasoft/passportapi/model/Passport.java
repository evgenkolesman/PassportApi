package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class Passport {

    private String id;

    private String number;

    private LocalDate givenDate;

    private String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(PassportRequest passportRequest) {
        Passport passport = ModelMapperMaker.configMapper().map(passportRequest, Passport.class);
        passport.setId(UUID.randomUUID().toString());
        return passport;
    }
}
