package com.lancea.studium.studium_api.exception;

import com.lancea.studium.studium_api.dto.response.single_response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    //Handles exceptions caught by Spring Validation
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request){
        Map<String, Object> results = new HashMap<>();

        System.out.println(ex.getBindingResult().getFieldErrors());

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            results.put(error.getField(), error.getDefaultMessage());
        });

        //Using the web request, check the endpoint that failed
        String path = request.getDescription(false);

        Map<String, Object> responseBody = new HashMap<>();

        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", status.value());
        responseBody.put("errors", results);
        responseBody.put("message", "Validation Failure");

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> resourceNotFoundExceptionResponseEntity
            (Exception ex, HttpServletRequest httpServletRequest){

        ExceptionResponse responseBody = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(),
                HttpStatus.NOT_FOUND.value(), httpServletRequest.getRequestURI());

        //Not found status
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> unauthorizeExceptionResponseEntity
            (Exception ex, HttpServletRequest httpServletRequest){

        ExceptionResponse responseBody = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(), httpServletRequest.getRequestURI());

        //Unauthorize status
        return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> responseStatusExceptionResponseEntity
            (ResponseStatusException ex, WebRequest request){
        ExceptionResponse responseBody = new ExceptionResponse(LocalDateTime.now(), ex.getReason(), ex.getStatusCode().value(), request.getDescription(false));

        //Status depends on the argument passed to it during initialization
        return new ResponseEntity<>(responseBody, ex.getStatusCode());  
    }

    @ExceptionHandler(InvalidSessionStateException.class)
    public ResponseEntity<ExceptionResponse> invalidSessionStateExceptionResponseEntity(Exception ex, HttpServletRequest servletRequest){
        ExceptionResponse exceptionBody = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), servletRequest.getRequestURI());

        return new ResponseEntity<>(exceptionBody, HttpStatus.UNAUTHORIZED);
    }

}
