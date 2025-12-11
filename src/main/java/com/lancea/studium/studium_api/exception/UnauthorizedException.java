package com.lancea.studium.studium_api.exception;

//Exception related to user authorization
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message){
        super(message);
    }
}
