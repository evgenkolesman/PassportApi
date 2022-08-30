package com.sperasoft.passportapi.exceptions.passportexceptions;


import java.io.Serial;

public class InvalidPassportDataException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5981199656447055431L;

    private static final String INVALID_PASSPORT = "Invalid passport data: Start date is after End date";

    public InvalidPassportDataException() {
        super(INVALID_PASSPORT);
    }


}
