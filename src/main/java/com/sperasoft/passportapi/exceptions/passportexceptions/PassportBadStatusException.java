package com.sperasoft.passportapi.exceptions.passportexceptions;

public class PassportBadStatusException extends RuntimeException {

    private static final String PASSPORT_STATUS = "Passport has another status";
    private static final long serialVersionUID = -2035939310785063161L;

    public PassportBadStatusException() {
        super(PASSPORT_STATUS);
    }
}
