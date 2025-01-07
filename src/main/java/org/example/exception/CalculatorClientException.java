package org.example.exception;

/**
 * Исключение при вызове CalculatorClient
 */
public class CalculatorClientException extends RuntimeException {
    public CalculatorClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
