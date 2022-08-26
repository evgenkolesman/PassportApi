package com.sperasoft.passportapi.exceptions.passportexceptions;

public class PassportNotFoundException extends RuntimeException {

    private static final String PASSPORT_NOT_FOUND = "Passport with ID: %s not found";
    private static final long serialVersionUID = -3578726135963953405L;

    public PassportNotFoundException(String message) {
        super(String.format(PASSPORT_NOT_FOUND, message));
    }

}
