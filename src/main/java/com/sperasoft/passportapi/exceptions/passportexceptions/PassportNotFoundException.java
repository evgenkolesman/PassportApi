package com.sperasoft.passportapi.exceptions.passportexceptions;

import java.io.Serial;

public class PassportNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3578726135963953405L;

    private static final String PASSPORT_NOT_FOUND = "Passport with ID: %s not found";

    public PassportNotFoundException(String passportId) {
        super(String.format(PASSPORT_NOT_FOUND, passportId));
    }

}
