package com.ciklum.ghl.controllers;

import com.ciklum.ghl.dto.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class GitHubExceptionHandler {

    private static HttpHeaders overrideContentType() {
        var httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        return httpHeaders;
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Error> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Error occurred during request", ex);
        Error error = new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Error> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        Error error =
                new Error(HttpStatus.NOT_ACCEPTABLE.value(), "Only " + MediaType.APPLICATION_JSON + " is supported");

        return new ResponseEntity<>(error, overrideContentType(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Error> handleHttpClientException(HttpClientErrorException ex) {
        if(ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            return new ResponseEntity<>(new Error(HttpStatus.NOT_FOUND.value(), "User not found!"),
                    HttpStatus.valueOf(HttpStatus.NOT_FOUND.value()));
        }

        return new ResponseEntity<>(new Error(ex.getStatusCode().value(), ex.getMessage()),
                HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}
