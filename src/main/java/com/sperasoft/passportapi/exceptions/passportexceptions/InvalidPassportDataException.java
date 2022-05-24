package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPassportDataException extends RuntimeException{

    private final static long serialVersionUID = -5981199656447055431L;

    public InvalidPassportDataException() {
        super();
    }

}
