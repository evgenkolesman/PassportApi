package com.sperasoft.passportapi.exceptions.personexceptions;

import java.io.Serial;

public class PersonNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5684387770921886370L;

    public static final String PERSON_NOT_FOUND = "Person with this ID: %s not found";

    public PersonNotFoundException(String personId) {
        super(String.format(PERSON_NOT_FOUND, personId));
    }
}
