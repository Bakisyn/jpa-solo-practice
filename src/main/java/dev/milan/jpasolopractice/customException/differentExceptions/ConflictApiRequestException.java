package dev.milan.jpasolopractice.customException.differentExceptions;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import org.springframework.http.HttpStatus;

public class ConflictApiRequestException extends ApiRequestException {
    public ConflictApiRequestException(String message) {
        super(message);
    }

    public ConflictApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    public static ConflictApiRequestException throwConflictApiRequestException(String message){
        throw new ConflictApiRequestException(message);
    }
}
