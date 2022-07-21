package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.model.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PersonResponse {

    @NonNull
    private final String id;

    @NonNull
    private final String name;

    @NonNull
    private final LocalDate birthday;

    @NonNull
    private final String birthdayCountry;

    private PersonResponse(Person person) {
        this.id = person.getId();
        this.name = person.getName();
        this.birthday = person.getBirthday();
        this.birthdayCountry = person.getBirthdayCountry();
    }

    public static PersonResponse of(@NonNull final Person person) {
        return new PersonResponse(person);
    }
}
