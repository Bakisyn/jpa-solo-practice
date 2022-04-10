package dev.milan.jpasolopractice.customException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;


@ControllerAdvice       //kaze da ovo hendluje exceptione
public class ApiRequestExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})  //kaze da ovaj metod hendluje automatski ovaj
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        String message;
        HttpStatus httpStatus;

        if (!e.getMessage().contains("-")){
            message = e.getMessage();
            httpStatus = badRequest;
        }else{
            String[] exceptionData = e.getMessage().split("/");
            message = exceptionData[0];
            try{
                httpStatus = HttpStatus.valueOf(Integer.parseInt(exceptionData[1]));
            }catch (IllegalArgumentException g){
                httpStatus = badRequest;
            }
        }

        ApiException apiException = new ApiException(
                message,
                e,
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, httpStatus);
    }
}
