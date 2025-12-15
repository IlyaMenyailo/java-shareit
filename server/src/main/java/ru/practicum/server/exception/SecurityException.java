package ru.practicum.server.exception;

public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }
}
