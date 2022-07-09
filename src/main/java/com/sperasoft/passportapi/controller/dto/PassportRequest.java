package com.sperasoft.passportapi.controller.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PassportRequest {

    @NotBlank(message = "Passport number field should be filled")
    @Digits(integer = 10, fraction = 0, message = "Invalid passport number")
    @NonNull
    private final String number;

    @NotBlank(message = "Given date field should be filled")
    @DateTimeFormat(iso = ISO.DATE)
    @NonNull
    private final Instant givenDate;

    @NotBlank(message = "Department code field should be filled")
    @Digits(integer = 6, fraction = 0, message = "Invalid department code")
    @NonNull
    private final String departmentCode;

    public PassportRequest(@NonNull String number, @NonNull Instant givenDate, @NonNull String departmentCode) {
        if (number.length() != 10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid passport number length must be 10 digits");
        this.number = number;
        this.givenDate = givenDate;
        if (departmentCode.length() != 6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid Department code length must be 6 digits");
        this.departmentCode = departmentCode;
    }
}
