package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PassportEmptyException extends RuntimeException {

    private final static long serialVersionUID = -4021682973497541018L;

    public PassportEmptyException(String personID) {
        super(personID);
    }

}
