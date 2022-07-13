package com.sperasoft.passportapi.exceptions;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.ErrorModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

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
    protected ResponseEntity<ErrorModel> handleConflict(
            RuntimeException ex) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        String propName = "exception." + ex.getClass().getSimpleName();
        String message;
        var errorId = FriendlyId.createFriendlyId();
        if (ex.getMessage() != null) {
            message = String.format(
                    environment.getProperty(propName),
                    ex.getMessage());
        } else message = Objects.requireNonNull(
                environment.getProperty(propName));
        log.error(String.format("%s %s", errorId, message));
        return new ResponseEntity<>(new ErrorModel(errorId, message, status), status);
    }
}
