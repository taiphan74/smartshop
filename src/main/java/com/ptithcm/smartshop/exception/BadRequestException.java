package com.ptithcm.smartshop.exception;

public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason));
    }
}
