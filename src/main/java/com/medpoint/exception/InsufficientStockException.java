// exception/InsufficientStockException.java
package com.medpoint.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String drug, int available, int requested) {
        super("Insufficient stock for '%s': requested %d, available %d"
                .formatted(drug, requested, available));
    }
}