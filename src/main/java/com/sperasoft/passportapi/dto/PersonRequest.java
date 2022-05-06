package com.sperasoft.passportapi.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Data
public class PersonRequest {

    @NotBlank(message = "Name field should be filled")
    @Size(min = 3, message = "name must be minimum 2 characters long")
    private String name;

    @NotBlank(message = "Date field should be filled")
    @DateTimeFormat(iso = ISO.DATE)
    private Date birthday;

    @NotBlank
    @Size(min = 2, max = 2, message = "Birthday country should be formatted like ISO CODE (2 characters)")
    private String birthdayCountry;

}
