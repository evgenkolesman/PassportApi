package com.sperasoft.passportapi.controller.dto;

import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PersonRequest {

    @NotBlank(message = "Name field should be filled")
    @Size(min = 3, message = "name must be minimum 2 characters long")
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

}
