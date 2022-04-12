package dev.milan.jpasolopractice.customException;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ApiException {

    private final String message;
    private final Throwable throwable;
    private final ZonedDateTime timestamp;

    public ApiException(String message, Throwable throwable, ZonedDateTime timestamp) {
        this.message = message;
        this.throwable = throwable;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }


    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
}
