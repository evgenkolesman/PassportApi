package com.sperasoft.passportapi.exceptions.passportexceptions;

public class PassportDeactivatedException extends RuntimeException {

    private static final String PASSPORT_DEACTIVATED = "Passport was already deactivated or not exist";
    private static final long serialVersionUID = 3337071209904500038L;

    public PassportDeactivatedException() {
        super(PASSPORT_DEACTIVATED);
    }

}
