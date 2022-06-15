package com.sperasoft.passportapi.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.sperasoft.passportapi.model.Person;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class PersonResponse {
    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @NonNull
    private final LocalDate birthday;
    @NonNull
    private final String birthdayCountry;

    public static PersonResponse of(Person person) {
        return new PersonResponse(person.getId(),
                person.getName(),
                person.getBirthday(),
                person.getBirthdayCountry());
    }
}
