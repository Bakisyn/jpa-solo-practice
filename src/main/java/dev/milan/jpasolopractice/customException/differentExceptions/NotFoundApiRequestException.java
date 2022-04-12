package dev.milan.jpasolopractice.customException.differentExceptions;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import org.springframework.http.HttpStatus;

public class NotFoundApiRequestException extends ApiRequestException {
    public NotFoundApiRequestException(String message) {
        super(message);
    }

    public NotFoundApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    protected HttpStatus getStatus(){
        return HttpStatus.NOT_FOUND;
    }

    public static NotFoundApiRequestException throwNotFoundException(String message){
        throw new NotFoundApiRequestException(message);
    }
}
