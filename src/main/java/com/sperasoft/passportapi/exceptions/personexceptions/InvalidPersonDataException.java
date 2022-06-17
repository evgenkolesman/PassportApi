package com.sperasoft.passportapi.exceptions.personexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPersonDataException extends RuntimeException{

    private final static long serialVersionUID = 8321027096125720088L;

    public InvalidPersonDataException() {
        super();
    }
}
