package com.lancea.studium.studium_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MyExceptionTranslationFilter implements AuthenticationEntryPoint, AccessDeniedHandler {

    //Authentication
    @Override
    public void commence (HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Throwable root = (Throwable) request.getAttribute("REQUEST_EXCEPTION");

        //Throw a custom message depending on the exception

        if (root instanceof ExpiredJwtException) {
            response.getWriter().write("""
            { "error": "Token has expired" }
        """);
        } else if(root instanceof JwtException){
            response.getWriter().write("""
            { "error": "You submitted a tampered token" }
        """);
        }
        else{
            response.getWriter().write("""
                {"error": "Access token required" }
        """);
        }

    }

    //Authorization
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("""
                {"error": "You're not authorized to access this end-point" }
               """);
    }

}
