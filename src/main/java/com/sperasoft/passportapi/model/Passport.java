package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Date;
import java.util.UUID;

@Data
public class Passport {

    private String id;

    private String number;

    private Date givenDate;

    private String departmentCode;

    private boolean active = true;

    private String description;

    public static Passport of(PassportRequest passportRequest) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Passport passport = model.map(passportRequest, Passport.class);
        passport.setId(UUID.randomUUID().toString());
        return passport;
    }
}
