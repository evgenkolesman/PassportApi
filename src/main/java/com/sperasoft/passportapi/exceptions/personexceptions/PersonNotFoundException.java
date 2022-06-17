package com.sperasoft.passportapi.exceptions.personexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends RuntimeException {

    private final static long serialVersionUID = -5684387770921886370L;

    public PersonNotFoundException(String message) {
        super(message);
    }
}
