package dev.milan.jpasolopractice.customException;

import org.springframework.http.HttpStatus;

public abstract class ApiRequestException extends RuntimeException{

    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    protected abstract HttpStatus getStatus();
}
