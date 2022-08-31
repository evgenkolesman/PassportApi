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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler {

    private final Environment environment;

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PassportNotFoundException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PersonNotFoundException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(InvalidPersonDataException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PassportWrongNumberException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PassportWasAddedException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PassportBadStatusException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(InvalidPassportDataException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(PassportDeactivatedException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId,
                exception.getMessage(),
                HttpStatus.CONFLICT),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(MethodArgumentNotValidException exception) {
        var errorId = FriendlyId.createFriendlyId();
        List<FieldError> fieldErrors = new ArrayList<>(exception
                .getFieldErrors());
        if (fieldErrors.size() > 1) {
            fieldErrors = fieldErrors.stream().sorted((first, second) ->
                    Optional.ofNullable(first.getCode()).orElseThrow().chars().toArray()[0]
                            >=
                            Optional.ofNullable(second.getCode())
                                    .orElseThrow().chars().toArray()[0] ? 0 : -1).collect(Collectors.toList());
        }
        String collectMessages = fieldErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining("\n"));
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        return new ResponseEntity<>(new ErrorModel(errorId, collectMessages, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(MethodArgumentTypeMismatchException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        String message = environment.getProperty("exception.BadDateFormat");
        return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorModel> handleException(HttpMessageNotReadableException exception) {
        var errorId = FriendlyId.createFriendlyId();
        log.info(String.format("%s %s", errorId, exception.getMessage()));
        String message = environment.getProperty("exception.BadDateFormat");
        return new ResponseEntity<>(new ErrorModel(errorId, message, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }
}
