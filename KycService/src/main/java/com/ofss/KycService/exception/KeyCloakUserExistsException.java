package com.ofss.KycService.exception;

public class KeyCloakUserExistsException extends RuntimeException {
    public KeyCloakUserExistsException() {}
    public KeyCloakUserExistsException(String message) {
        super(message);
    }
}