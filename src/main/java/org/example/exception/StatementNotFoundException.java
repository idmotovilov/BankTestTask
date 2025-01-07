package org.example.exception;

/**
 * Исключение при отсутствии Statement в базе.
 */
public class StatementNotFoundException extends RuntimeException {
    public StatementNotFoundException(String message) {
        super(message);
    }
}
