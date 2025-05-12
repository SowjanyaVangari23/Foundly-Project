package com.foundly.app2.exception;

public class ItemNotFoundException extends RuntimeException  {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    public ItemNotFoundException(String message) {
        super(message);
    }
}