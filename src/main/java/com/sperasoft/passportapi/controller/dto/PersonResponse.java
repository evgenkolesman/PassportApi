package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.model.Person;
import lombok.Data;
import lombok.NonNull;

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
