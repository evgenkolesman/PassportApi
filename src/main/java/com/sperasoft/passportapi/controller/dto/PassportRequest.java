package com.sperasoft.passportapi.controller.dto;

import lombok.Data;
import lombok.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PassportRequest {

    @NotBlank(message = "Passport number field should be filled")
    @Size(min = 10, max = 10)
    @NonNull
    private final String number;

    @DateTimeFormat(iso = ISO.DATE)
    @NonNull
    private final Instant givenDate;

    @NotBlank(message = "Department code field should be filled")
    @Digits(integer = 6, fraction = 0, message = "Invalid department code")
    @Size(min = 6, max = 6)
    @NonNull
    private final String departmentCode;

    public PassportRequest(@NonNull String number, @NonNull Instant givenDate, @NonNull String departmentCode) {
        this.number = number;
        this.givenDate = givenDate.truncatedTo(ChronoUnit.MICROS);
        this.departmentCode = departmentCode;
    }

}
