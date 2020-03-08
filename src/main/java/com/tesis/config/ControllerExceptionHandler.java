package com.tesis.config;

import com.tesis.exceptions.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ApiError> noHandlerFoundException(HttpServletRequest req, NoHandlerFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), String.format("Route %s not found", req.getRequestURI()));
        return ResponseEntity.status(apiError.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    protected ResponseEntity<ApiError> handleApiException(ResponseStatusException e) {
        Integer statusCode = e.getStatus().value();
        boolean expected = 500 > statusCode;
        if (expected) {
            logger.warn("[message: {}] [status: {}] [stackTrace: {}]", e.getReason(), e.getStatus().value(), e.getStackTrace());
        } else {
            logger.error("[message: {}] [status: {}] [stackTrace: {}]", e.getReason(), e.getStatus().value(), e.getStackTrace());
        }

        ApiError apiError = new ApiError(statusCode, e.getReason());
        return ResponseEntity.status(apiError.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(value = {ServletRequestBindingException.class})
    protected ResponseEntity<ApiError> handleServletRequestBindingException(ServletRequestBindingException e) {
        logger.error("[message: {}] [status: 500] [stackTrace: {}]", e.getMessage(), e.getStackTrace());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.status(apiError.getStatus())
                .body(apiError);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ApiError> handleUnknownException(Exception e) {
        logger.error("[message: {}] [status: 500] [stackTrace: {}]", e.getMessage(), e.getStackTrace());

        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        return ResponseEntity.status(apiError.getStatus())
                .body(apiError);
    }
}