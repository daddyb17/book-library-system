package com.collabera.booklibrarysystem.exception;

public class DuplicateResourceException extends BusinessConflictException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
