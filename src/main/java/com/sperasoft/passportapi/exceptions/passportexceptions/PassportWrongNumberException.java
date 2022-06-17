package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PassportWrongNumberException extends RuntimeException{

    private final static long serialVersionUID = -783573036046978512L;

    public PassportWrongNumberException() {
        super();
    }
}
