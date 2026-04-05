package com.ptithcm.smartshop.shared.exception;

public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String resource, String field) {
        super(String.format("%s with %s already exists", resource, field));
    }
}

