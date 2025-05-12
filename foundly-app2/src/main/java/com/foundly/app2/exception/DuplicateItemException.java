package com.foundly.app2.exception;


public class DuplicateItemException extends RuntimeException  {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    public DuplicateItemException(String message) {
        super(message);
    }
}