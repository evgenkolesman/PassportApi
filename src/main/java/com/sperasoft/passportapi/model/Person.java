package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.controller.dto.PersonRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Person {

    private final String id;

    private final String name;

    private final LocalDate birthday;

    private final String birthdayCountry;

    private List<Passport> list = new ArrayList<>();

    public static Person of(final String id, final PersonRequest personRequest) {
        return new Person(id,
                personRequest.getName(),
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
    }
}
