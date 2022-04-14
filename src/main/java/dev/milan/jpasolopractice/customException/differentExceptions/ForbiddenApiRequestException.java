package dev.milan.jpasolopractice.customException.differentExceptions;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import org.springframework.http.HttpStatus;

public class ForbiddenApiRequestException extends ApiRequestException {
    public ForbiddenApiRequestException(String message) {
        super(message);
    }

    public ForbiddenApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    protected HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

    public static ForbiddenApiRequestException throwForbiddenApiRequestException(String message){
        throw new ForbiddenApiRequestException(message);
    }
}
