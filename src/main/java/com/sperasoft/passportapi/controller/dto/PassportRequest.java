package com.sperasoft.passportapi.controller.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PassportRequest {

    @NotNull(message = "Invalid data: Passport number field should be filled")
    @Size(min = 10, max = 10, message = "Invalid data: Passport number should be 10 symbols length")
    private final String number;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    @NotNull(message = "Invalid data: Given Date field shouldn`t be empty")
    private final Instant givenDate;

    @NotNull(message = "Invalid data: Department code field should be filled")
    @Digits(integer = 6, fraction = 6, message = "Invalid data: Invalid department code")
    @Size(min = 6, message = "Invalid data: department code size should be 6 digits")
    private final String departmentCode;
}
