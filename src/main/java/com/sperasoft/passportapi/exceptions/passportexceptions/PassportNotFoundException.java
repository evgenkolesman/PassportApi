package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PassportNotFoundException extends RuntimeException {

    private final static long serialVersionUID = -3578726135963953405L;

    public PassportNotFoundException(String message) {
        super(message);
    }

}
