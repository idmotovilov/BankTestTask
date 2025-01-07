package org.example.exception;

/**
 * Исключение на уровне контроллера
 */
public class DealControllerException extends RuntimeException {
    public DealControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}
