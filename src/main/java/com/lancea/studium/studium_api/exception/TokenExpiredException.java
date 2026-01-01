package com.lancea.studium.studium_api.exception;

import org.springframework.security.core.AuthenticationException;

/*
Custom Expired token exception that will be thrown if the JwtAuthenticationFilter detects a ExpiredJwtToken Exception
 */
public class TokenExpiredException extends AuthenticationException {

    public TokenExpiredException(String message){
        super(message);
    }
}
