package com.sperasoft.passportapi.controller.dto;

import lombok.Data;

@Data
public class TestErrorModel {
    private final String id;
    private final String message;
    private final String status;
}