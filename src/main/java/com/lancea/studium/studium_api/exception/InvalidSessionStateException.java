package com.lancea.studium.studium_api.exception;

public class InvalidSessionStateException extends RuntimeException{
    public InvalidSessionStateException(String message){
        super(message);
    }
}
