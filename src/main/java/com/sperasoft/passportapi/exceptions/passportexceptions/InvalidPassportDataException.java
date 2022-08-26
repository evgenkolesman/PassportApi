package com.sperasoft.passportapi.exceptions.passportexceptions;


public class InvalidPassportDataException extends RuntimeException {

    private static final String INVALID_PASSPORT = "Invalid passport data: Start date is after End date";
    private static final long serialVersionUID = -5981199656447055431L;

    public InvalidPassportDataException() {
        super(INVALID_PASSPORT);
    }


}
