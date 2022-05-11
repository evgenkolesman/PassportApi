package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.dto.PassportRequest;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

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
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Passport passport = model.map(passportRequest, Passport.class);
        passport.setId(UUID.randomUUID().toString());
        return passport;
    }
}
