package com.lancea.studium.studium_api.controller;


import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.dto.response.single_response.AuthResponse;
import com.lancea.studium.studium_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;


@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }


    //Account Registration Controller
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> create(
            @Validated @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response, @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent){

        //Delegate the creation of the new user to the service method
        long newUserId = authService.createUser(registerRequest, response, userAgent);

        //Build location URI for the new resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/{id}")
                .buildAndExpand(newUserId)
                .toUri();

        //Return ResponseEntity with 201 status
        return ResponseEntity.created(location).body(new AuthResponse("Account created successfully"));
    }

    //Login Controller
    @PostMapping("/login")
    public ResponseEntity<?> verifyUser(
            @Validated @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent){

        //Delegate the verification to the service
        authService.verifyCredentials(loginRequest, response, userAgent);


        return ResponseEntity.ok().body(new AuthResponse("Login Successful"));

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSpecificUser(@PathVariable Long userId){
        Map<String, Object> authResponseBody = authService.getSpecificUser(userId);

        return ResponseEntity.ok(authResponseBody);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(HttpServletRequest request,
                                               HttpServletResponse response, @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent){

        authService.generateNewRefreshToken(request, response, userAgent);
        return ResponseEntity.ok("Token generation successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){
        authService.logoutUser(request, response);
        return ResponseEntity.ok("Logout Successfully");
    }


}
