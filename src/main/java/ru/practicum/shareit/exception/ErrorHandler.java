package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<String> handleValidationException(NoSuchElementException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotExistException(AlreadyExistException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleItemNotAvailableException(ItemNotAvailableException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedStatusException(UnsupportedStatusException e) {
        log.error(e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @Getter
    @Setter
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
