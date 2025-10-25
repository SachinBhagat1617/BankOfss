package com.ofss.Account.exception;

public class KeyCloakUserExistsException extends RuntimeException {
    public KeyCloakUserExistsException() {}
    public KeyCloakUserExistsException(String message) {
        super(message);
    }
}