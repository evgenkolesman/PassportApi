package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        Person person = ModelMapperMaker.configMapper().map(personRequest, Person.class);
        person.setId(UUID.randomUUID().toString());
        return person;
    }
}
