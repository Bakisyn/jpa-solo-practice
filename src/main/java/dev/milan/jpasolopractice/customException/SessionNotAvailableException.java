package dev.milan.jpasolopractice.customException;

public class SessionNotAvailableException extends Exception{
    public SessionNotAvailableException(String message){
        super(message);
    }
}
