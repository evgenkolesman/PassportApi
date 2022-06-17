package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PassportWasAddedException extends RuntimeException{

    private final static long serialVersionUID = 3106756833114446303L;

    public PassportWasAddedException() {
        super();
    }
}
