package com.foundly.app2.exception;

public class TransactionNotFoundException extends RuntimeException  {
    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    public TransactionNotFoundException(String message) {
        super(message);
    }
}