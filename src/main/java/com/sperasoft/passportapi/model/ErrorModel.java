package com.sperasoft.passportapi.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorModel {
    private final String id;
    private final String message;
    private final HttpStatus status;

}
