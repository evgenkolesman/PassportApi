package com.sperasoft.passportapi.exceptions.personexceptions;

import java.io.Serial;

public class InvalidPersonDataException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8321027096125720088L;

    public static final String INVALID_PERSON_DATA = "Invalid person data";

    public InvalidPersonDataException() {
        super(INVALID_PERSON_DATA);
    }
}
