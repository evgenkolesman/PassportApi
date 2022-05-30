package com.sperasoft.passportapi.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.model.Person;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonResponse {

    private String id;

    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthday;

    private String birthdayCountry;

    public static PersonResponse of(Person person) {
        return ModelMapperMaker.configMapper().map(person, PersonResponse.class);
    }
}
