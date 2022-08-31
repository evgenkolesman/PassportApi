package com.sperasoft.passportapi.exceptions.passportexceptions;

import java.io.Serial;

public class PassportDeactivatedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3337071209904500038L;

    private static final String PASSPORT_DEACTIVATED = "Passport was already deactivated or not exist";

    public PassportDeactivatedException() {
        super(PASSPORT_DEACTIVATED);
    }

}
