package com.sperasoft.passportapi.controller.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PersonRequest {

    @NotBlank(message = "Name field should be filled")
    @Size(min = 2, message = "name must be minimum 2 characters long")
    private final String name;

    @DateTimeFormat(iso = ISO.DATE)
    @NotNull(message = "Given Date field shouldn`t be empty")
    private final LocalDate birthday;

    @NotBlank(message = "BirthdayCountry field should be filled")
    @Size(min = 2, max = 2, message = "Birthday country should be formatted like ISO CODE (2 characters)")
    private final String birthdayCountry;
}
