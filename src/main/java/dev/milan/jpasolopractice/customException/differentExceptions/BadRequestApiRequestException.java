package dev.milan.jpasolopractice.customException.differentExceptions;

import dev.milan.jpasolopractice.customException.ApiRequestException;
import org.springframework.http.HttpStatus;

public class BadRequestApiRequestException extends ApiRequestException {
    public BadRequestApiRequestException(String message) {
        super(message);
    }

    public BadRequestApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    protected HttpStatus getStatus(){
        return HttpStatus.BAD_REQUEST;
    }

    public static BadRequestApiRequestException throwBadRequestException(String message){
        throw new BadRequestApiRequestException(message);
    }
}
