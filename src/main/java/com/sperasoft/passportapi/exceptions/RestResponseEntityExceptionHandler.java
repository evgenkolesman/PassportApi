package com.sperasoft.passportapi.exceptions;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.ErrorModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler(value = {
            InvalidPassportDataException.class,
            PassportDeactivatedException.class,
            PassportNotFoundException.class,
            PassportWasAddedException.class,
            PassportWrongNumberException.class,
            PassportBadStatusException.class,
            PersonNotFoundException.class,
            InvalidPersonDataException.class
    })
    protected ResponseEntity<ErrorModel> handleException(
            RuntimeException ex) {
        var errorId = FriendlyId.createFriendlyId();
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

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(MethodArgumentNotValidException exception) {
        var errorId = FriendlyId.createFriendlyId();
        List<FieldError> fieldErrors = new ArrayList<>(exception
                .getFieldErrors());
        if (fieldErrors.size() > 1) {
            fieldErrors = fieldErrors.stream().sorted((first, second) ->
                    first.getCode().chars().toArray()[0]
                            >=
                            second.getCode().chars().toArray()[0] ? 0 : -1).collect(Collectors.toList());
        }
        String collectMessages = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("\n"));
        return new ResponseEntity<>(new ErrorModel(errorId, collectMessages, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(MethodArgumentTypeMismatchException exception) {
        var errorId = FriendlyId.createFriendlyId();
        String message = environment.getProperty("exception.BadDateFormat");
        return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(HttpMessageNotReadableException exception) {
        var errorId = FriendlyId.createFriendlyId();
        String message = environment.getProperty("exception.BadDateFormat");
        return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);

    }


}
