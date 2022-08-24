package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<String> handleValidationException (NoSuchElementException e) {
        log.error (e.getMessage ());
        return new ResponseEntity<> (e.getMessage (), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotExistException (AlreadyExistException e) {
        log.error (e.getMessage ());
        return new ResponseEntity<> (e.getMessage (), HttpStatus.CONFLICT);
    }
}