package com.sperasoft.passportapi.exceptions;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.ErrorModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(value = {MethodArgumentNotValidException.class,
            InvalidPassportDataException.class,
            PassportDeactivatedException.class,
            PassportNotFoundException.class,
            PassportWasAddedException.class,
            PassportWrongNumberException.class,
            PassportBadStatusException.class,
            PersonNotFoundException.class,
            InvalidPersonDataException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class

    })
    protected ResponseEntity<ErrorModel> handleConflict(
            Exception ex) {
        var errorId = FriendlyId.createFriendlyId();
        if (ex instanceof MethodArgumentNotValidException) {
            return new ResponseEntity<>(new ErrorModel(errorId, ((MethodArgumentNotValidException) ex)
                    .getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            String message = environment.getProperty("exception.BadDateFormat");
            return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        } else if (ex instanceof HttpMessageNotReadableException) {
            String message = environment.getProperty("exception.BadDateFormat");
            return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                    HttpStatus.BAD_REQUEST);
        }
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        String propName = "exception." + ex.getClass().getSimpleName();
        String message;

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
