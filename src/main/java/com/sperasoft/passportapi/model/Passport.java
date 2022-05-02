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

    String id;

    String number;

    Date givenDate;

    String departmentCode;

    boolean active = true;

    public static Passport of(PassportRequest passportRequest) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Passport passport = model.map(passportRequest, Passport.class);
        passport.setId(UUID.randomUUID().toString());
        return passport;
    }
}
