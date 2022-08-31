package com.sperasoft.passportapi.exceptions.passportexceptions;

import java.io.Serial;

public class PassportBadStatusException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2035939310785063161L;

    private static final String PASSPORT_STATUS = "Passport has another status";

    public PassportBadStatusException() {
        super(PASSPORT_STATUS);
    }
}
