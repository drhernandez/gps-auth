package com.tesis.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ForbiddenException extends ResponseStatusException {

    public ForbiddenException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }

    public ForbiddenException(String reason, Throwable cause) {
        super(HttpStatus.FORBIDDEN, reason, cause);
    }
}
