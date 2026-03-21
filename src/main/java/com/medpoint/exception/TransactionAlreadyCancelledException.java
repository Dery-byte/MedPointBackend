package com.medpoint.exception;

public class TransactionAlreadyCancelledException extends RuntimeException {
    public TransactionAlreadyCancelledException(String message) {
        super(message);
    }
}
