package com.sperasoft.passportapi.exceptions.passportexceptions;

import java.io.Serial;

public class PassportWrongNumberException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -783573036046978512L;

    private static final String WRONG_NUMBER = "Wrong number";

    public PassportWrongNumberException() {
        super(WRONG_NUMBER);
    }
}
