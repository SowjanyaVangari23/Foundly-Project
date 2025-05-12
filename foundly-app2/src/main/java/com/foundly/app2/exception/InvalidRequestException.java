package com.foundly.app2.exception;

public class InvalidRequestException extends RuntimeException  {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    public InvalidRequestException(String message) {
        super(message);
    }
}