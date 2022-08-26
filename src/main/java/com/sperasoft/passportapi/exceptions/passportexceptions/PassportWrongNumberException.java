package com.sperasoft.passportapi.exceptions.passportexceptions;

public class PassportWrongNumberException extends RuntimeException {

    private static final String WRONG_NUMBER = "Wrong number";
    private static final long serialVersionUID = -783573036046978512L;

    public PassportWrongNumberException() {
        super(WRONG_NUMBER);
    }
}
