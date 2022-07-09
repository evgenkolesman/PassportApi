package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PersonRequest {

    @NotBlank(message = "Name field should be filled")
    @Size(min = 2, message = "name must be minimum 2 characters long")
    @NonNull
    private final String name;

    @NotBlank(message = "Date field should be filled")
    @DateTimeFormat(iso = ISO.DATE)
    @NonNull
    private final LocalDate birthday;

    @NotBlank
    @Size(min = 2, max = 2, message = "Birthday country should be formatted like ISO CODE (2 characters)")
    @NonNull
    private final String birthdayCountry;

    public PersonRequest(@NonNull String name, @NonNull LocalDate birthday, @NonNull String birthdayCountry) {
        if (name.length() < 3) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must be minimum 2 characters long");
        this.name = name;
        this.birthday = birthday;
        if (birthdayCountry.length() != 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Birthday country should be formatted like ISO CODE (2 characters)");
        this.birthdayCountry = birthdayCountry;
    }
}
