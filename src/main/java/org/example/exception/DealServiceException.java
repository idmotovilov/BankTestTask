package org.example.exception;

/**
 * Исключение, которое может пробрасывать DealService
 */
public class DealServiceException extends RuntimeException {
    public DealServiceException(String message) {
        super(message);
    }

    public DealServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
