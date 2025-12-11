package com.lancea.studium.studium_api.exception;

//For any exception related to missing/not found data
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){
        super(message);
    }
}
