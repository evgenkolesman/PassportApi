package com.sperasoft.passportapi.exceptions;

import com.sperasoft.passportapi.configuration.EnvConfig;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;
import java.util.UUID;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final EnvConfig environment;

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
            RuntimeException ex,
            WebRequest request) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        String propName = "exception." + ex.getClass().getSimpleName();
        String message;
        if(ex.getMessage() != null) {
            message = String.format(
                    environment.getProperty(propName), ex.getMessage());
        } else message = Objects.requireNonNull(
                environment.getProperty(propName));
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

}