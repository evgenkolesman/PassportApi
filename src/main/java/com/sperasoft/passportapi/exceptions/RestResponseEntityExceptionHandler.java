package com.sperasoft.passportapi.exceptions;

import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    Environment environment;

    @ExceptionHandler(value
            = {RuntimeException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String message = null;
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        
        final Exceptions enumValue = Arrays.stream(Exceptions.values())
                .filter(a -> a.getExceptionClass().equals(ex.getClass())).findFirst().get();
        switch (enumValue) {
            case INVALID_PASSPORT_DATA_EXCEPTION:
                message = Objects.requireNonNull(environment.getProperty("passport.exception.invalid.date"));
                break;
            case PASSPORT_DEACTIVATED_EXCEPTION:
                message = Objects.requireNonNull(environment.getProperty("passport.exception.deactivated"));
                break;
            case PASSPORT_EMPTY_EXCEPTION:
                message = String.format(Objects.requireNonNull(
                        environment.getProperty("passport.exception.person.nopassport")), ex.getMessage());
                break;
            case PASSPORT_NOT_FOUND_EXCEPTION:
                message = String.format(Objects.requireNonNull(
                        environment.getProperty("passport.exception.notfound")), ex.getMessage());
                break;
            case PASSPORT_WAS_ADDED_EXCEPTION:
                message = Objects.requireNonNull(environment.getProperty("passport.exception.was-added"));
                break;
            case PASSPORT_WRONG_NUMBER_EXCEPTION:
                message = Objects.requireNonNull(environment.getProperty("searches.exception.wrong-num"));
                break;
            case PERSON_NOT_FOUND_EXCEPTION:
                message = String.format(Objects.requireNonNull(environment.getProperty("person.exception.notfound")),
                        ex.getMessage());
                break;
            case INVALID_PERSON_DATA_EXCEPTION:
                message = Objects.requireNonNull(environment.getProperty("person.exception.invalid-data"));
                break;
            default:
                break;
        }

        return getObjectResponseEntity(ex, request, message, status);
    }

    private ResponseEntity<Object> getObjectResponseEntity(RuntimeException ex,
                                                           WebRequest request,
                                                           String message,
                                                           HttpStatus status) {
        log.error(String.format("%s %s %s",
                UUID.randomUUID(),
                status,
                message
        ));
        return handleExceptionInternal(ex, message,
                new HttpHeaders(), status, request);
    }

    private enum Exceptions {
        INVALID_PASSPORT_DATA_EXCEPTION(InvalidPassportDataException.class),
        PASSPORT_DEACTIVATED_EXCEPTION(PassportDeactivatedException.class),
        PASSPORT_EMPTY_EXCEPTION(PassportEmptyException.class),
        PASSPORT_NOT_FOUND_EXCEPTION(PassportNotFoundException.class),
        PASSPORT_WAS_ADDED_EXCEPTION(PassportWasAddedException.class),
        PASSPORT_WRONG_NUMBER_EXCEPTION(PassportWrongNumberException.class),
        PERSON_NOT_FOUND_EXCEPTION(PersonNotFoundException.class),
        INVALID_PERSON_DATA_EXCEPTION(InvalidPersonDataException.class);

        private final Class<?> passportWasAddedExceptionClass;

        Exceptions(Class<?> passportWasAddedExceptionClass) {
            this.passportWasAddedExceptionClass = passportWasAddedExceptionClass;
        }

        public Class<?> getExceptionClass() {
            return passportWasAddedExceptionClass;
        }
    }


}
