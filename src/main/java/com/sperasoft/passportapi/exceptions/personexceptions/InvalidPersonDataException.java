package com.sperasoft.passportapi.exceptions.personexceptions;

public class InvalidPersonDataException extends RuntimeException {

    public static final String INVALID_PERSON_DATA = "Invalid person data";
    private static final long serialVersionUID = 8321027096125720088L;

    public InvalidPersonDataException() {
        super(INVALID_PERSON_DATA);
    }
}
