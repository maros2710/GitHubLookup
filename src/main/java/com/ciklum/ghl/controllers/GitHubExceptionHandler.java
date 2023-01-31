package com.ciklum.ghl.controllers;

import com.ciklum.ghl.dto.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GitHubExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Error> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Error occurred during request", ex);
        Error error = new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Error error =
                new Error(HttpStatus.NOT_ACCEPTABLE.value(), "Only " + MediaType.APPLICATION_JSON + " is supported");

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(error);
            return new ResponseEntity<>(json, HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception ignored) {

        }

       return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
    }

    @ResponseBody
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Error> handleHttpClientException(HttpClientErrorException ex) {
        return new ResponseEntity<>(new Error(ex.getStatusCode().value(), ex.getMessage()),
                HttpStatus.valueOf(ex.getStatusCode().value()));
    }
}
