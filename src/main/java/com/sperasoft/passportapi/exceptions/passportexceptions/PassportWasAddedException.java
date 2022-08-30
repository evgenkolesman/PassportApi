package com.sperasoft.passportapi.exceptions.passportexceptions;

import java.io.Serial;

public class PassportWasAddedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3106756833114446303L;

    private static final String PASSPORT_WAS_ADDED = "This passport was already added";

    public PassportWasAddedException() {
        super(PASSPORT_WAS_ADDED);
    }
}
