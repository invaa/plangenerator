package com.lendico.api.controller;

import com.lendico.api.dto.Error;
import com.lendico.api.exception.InvalidPlanParametersException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
@ControllerAdvice
public class ExceptionHandlerConfiguration extends ResponseEntityExceptionHandler {

    private ResponseEntity<Error> handleException(final Exception exception, final HttpStatus status) {
        Error response = new Error(LocalDateTime.now(), status.value(), exception.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(final Exception exception) {
        log.error(exception.getMessage(), exception);
        return handleException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidPlanParametersException.class)
    public ResponseEntity<Error> handleException(final InvalidPlanParametersException exception) {
        log.error(exception.getMessage(), exception);
        return handleException(exception, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException exception,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request
    ) {
        return new ResponseEntity(handleException(exception, HttpStatus.BAD_REQUEST).getBody(), HttpStatus.BAD_REQUEST);
    }
}
