package com.sperasoft.passportapi.exceptions;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Objects;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(value = {InvalidPassportDataException.class,
            PassportDeactivatedException.class,
            PassportEmptyException.class,
            PassportNotFoundException.class,
            PassportWasAddedException.class,
            PassportWrongNumberException.class,
            PassportBadStatusException.class,
            PersonNotFoundException.class,
            InvalidPersonDataException.class
    })
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        String propName = "exception." + ex.getClass().getSimpleName();
        String message;
        if (ex.getMessage() != null) {
            message = String.format(
                    environment.getProperty(propName), ex.getMessage());
        } else message = Objects.requireNonNull(
                environment.getProperty(propName));
        log.error(String.format("%s %s %s",
                FriendlyId.createFriendlyId(),
                status,
                message
        ));
        return new ResponseEntity<>(message, new HttpHeaders(), status);
    }
}
