package com.sperasoft.passportapi.model;

import com.sperasoft.passportapi.controller.dto.PersonRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Person {
    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @NonNull
    private final LocalDate birthday;
    @NonNull
    private final String birthdayCountry;

    private final List<Passport> list;

    public Person(@NonNull String id,
                  @NonNull String name,
                  @NonNull LocalDate birthday,
                  @NonNull String birthdayCountry) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.birthdayCountry = birthdayCountry;
        this.list = new ArrayList<>();
    }

    private Person(String id, PersonRequest personRequest) {
        this.id = id;
        this.name = personRequest.getName();
        this.birthday = personRequest.getBirthday();
        this.birthdayCountry = personRequest.getBirthdayCountry();
        this.list = new ArrayList<>();
    }

    public static Person of(final String id, final PersonRequest personRequest) {
        return new Person(id,
                personRequest);
    }
}
