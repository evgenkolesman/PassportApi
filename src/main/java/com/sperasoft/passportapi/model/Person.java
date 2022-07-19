package com.sperasoft.passportapi.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

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

    public Person(@NonNull String id,
                  @NonNull String name,
                  @NonNull LocalDate birthday,
                  @NonNull String birthdayCountry) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.birthdayCountry = birthdayCountry;
    }
}
