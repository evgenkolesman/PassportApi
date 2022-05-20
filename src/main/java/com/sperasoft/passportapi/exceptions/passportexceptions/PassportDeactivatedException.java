package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PassportDeactivatedException extends RuntimeException {

    private final static long serialVersionUID = 3337071209904500038L;

    public PassportDeactivatedException() {
        super();
    }

}
