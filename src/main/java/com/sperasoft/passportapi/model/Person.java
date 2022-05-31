package com.sperasoft.passportapi.model;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Person {

    private String id;

    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthday;

    private String birthdayCountry;

    private List<Passport> list = new ArrayList<>();

    public static Person of(PersonRequest personRequest) {
        Person person = ModelMapperMaker.configMapper().map(personRequest, Person.class);
        person.setId(FriendlyId.createFriendlyId());
        return person;
    }
}
