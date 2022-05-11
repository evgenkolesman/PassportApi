package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.dto.PersonRequest;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Person {

    private String id;

    private String name;

    private LocalDate birthday;

    private String birthdayCountry;

    private List<Passport> list = new ArrayList<>();

    public static Person of(PersonRequest personRequest) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Person person = model.map(personRequest, Person.class);
        person.setId(UUID.randomUUID().toString());
        return person;
    }
}
