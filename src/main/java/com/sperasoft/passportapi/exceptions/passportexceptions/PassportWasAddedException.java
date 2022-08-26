package com.sperasoft.passportapi.exceptions.passportexceptions;

public class PassportWasAddedException extends RuntimeException {

    private static final String PASSPORT_WAS_ADDED = "This passport was already added";
    private static final long serialVersionUID = 3106756833114446303L;

    public PassportWasAddedException() {
        super(PASSPORT_WAS_ADDED);
    }
}
