package com.sperasoft.passportapi.exceptions.personexceptions;

public class PersonNotFoundException extends RuntimeException {

    public static final String PERSON_NOT_FOUND = "Person with this ID: %s not found";
    private static final long serialVersionUID = -5684387770921886370L;

    public PersonNotFoundException(String message) {
        super(String.format(PERSON_NOT_FOUND, message));
    }
}
