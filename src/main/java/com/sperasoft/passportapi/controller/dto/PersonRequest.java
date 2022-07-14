package com.sperasoft.passportapi.controller.dto;

import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PersonRequest {

    @NotBlank(message = "Name field should be filled")
    @Size(min = 2, message = "name must be minimum 2 characters long")
    @NonNull
    private final String name;

    @DateTimeFormat(iso = ISO.DATE)
    @NonNull
    private final LocalDate birthday;

    @NotBlank(message = "BirthdayCountry field should be filled")
    @Size(min = 2, max = 2, message = "Birthday country should be formatted like ISO CODE (2 characters)")
    @NonNull
    private final String birthdayCountry;
}
