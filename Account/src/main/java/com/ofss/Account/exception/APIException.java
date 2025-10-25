package com.ofss.Account.exception;

import org.springframework.http.HttpStatus;


public class APIException extends RuntimeException {
    private final HttpStatus status;

    public APIException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public APIException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
