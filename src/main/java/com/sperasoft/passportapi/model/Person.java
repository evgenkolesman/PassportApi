package com.sperasoft.passportapi.model;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.configuration.CommonConfiguration;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Person {

    private String id;

    private String name;

    private LocalDate birthday;

    private String birthdayCountry;

    private List<Passport> list = new ArrayList<>();

    public static Person of(PersonRequest personRequest) {
        Person person = CommonConfiguration.configMapper().map(personRequest, Person.class);
        person.setId(FriendlyId.createFriendlyId());
        return person;
    }
}
