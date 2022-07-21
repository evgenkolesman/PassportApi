package com.sperasoft.passportapi.controller.dto;

import lombok.Data;

@Data
public class PassportRequestTest {

    private final String number;

    private final String givenDate;

    private final String departmentCode;
}
