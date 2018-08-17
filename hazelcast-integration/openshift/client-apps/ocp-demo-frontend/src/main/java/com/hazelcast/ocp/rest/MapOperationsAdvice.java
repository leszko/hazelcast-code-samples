package com.hazelcast.ocp.rest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class MapOperationsAdvice {
    private static final Logger log = Logger.getLogger(MapOperations.class);

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {IllegalStateException.class})
    protected void handleMapOperationsException(RuntimeException ex) {
        log.error("Exception forwarded to client {}", ex.getCause());
    }
}
