package com.sperasoft.passportapi.exceptions.passportexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PassportBadStatusException extends RuntimeException {

    private final static long serialVersionUID = -2035939310785063161L;
}
