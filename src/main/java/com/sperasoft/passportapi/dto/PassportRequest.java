package com.sperasoft.passportapi.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.util.Date;

import static org.springframework.format.annotation.DateTimeFormat.*;

@Data
public class PassportRequest {

    @NotBlank(message ="Passport number field should be filled")
    @Digits(integer = 10, fraction = 0, message = "Invalid passport number")
    String number;

    @NotBlank(message ="Given date field should be filled")
    @DateTimeFormat(iso = ISO.DATE)
    Date givenDate;

    @NotBlank(message ="Department code field should be filled")
    @Digits(integer = 6, fraction = 0, message = "Invalid department code")
    String departmentCode;
}
