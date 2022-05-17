package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.model.Person;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonResponse {

    private String id;

    private String name;

    private LocalDate birthday;

    private String birthdayCountry;

    public static PersonResponse of(Person personStore) {
        return ModelMapperMaker.configMapper().map(personStore, PersonResponse.class);
    }
}
