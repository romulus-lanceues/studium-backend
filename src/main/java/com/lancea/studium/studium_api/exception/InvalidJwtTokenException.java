package com.lancea.studium.studium_api.exception;

import org.springframework.security.core.AuthenticationException;

/*
Custom Invalid token exception that will be thrown if the JwtAuthenticationFilter detects an Exception regarding the token (e.g. tampered)
 */
public class InvalidJwtTokenException extends AuthenticationException {

    public InvalidJwtTokenException(String message){
        super(message);
    }
}
