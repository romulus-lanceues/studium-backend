package com.lancea.studium.studium_api.controller;


import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RefreshTokenRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.dto.response.AuthResponse;
import com.lancea.studium.studium_api.dto.response.NewRefreshTokenResponse;
import com.lancea.studium.studium_api.service.AuthService;
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

    //Random Endpoint to test if the API is up and running
    @GetMapping("/test")
    public AuthResponse apiTest(){
        return new AuthResponse(000L, "API up and working", "studium API", null,  null);
    }

    //Account Registration Controller
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> create(
           @Validated @RequestBody RegisterRequest registerRequest){

        //Delegate the creation of new user to the service method
        AuthResponse createdUser = authService.createUser(registerRequest);

        //Build location URI for new resource
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        //Return ResponseEntity with 201 status
        return ResponseEntity.created(location).body(createdUser);
    }

    //Login Controller
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> verifyUser(
            @Validated @RequestBody LoginRequest loginRequest){

        //Delegate the verification to the service
        AuthResponse verifyInfo = authService.verifyCredentials(loginRequest);

        return ResponseEntity.ok().body(verifyInfo);

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSpecificUser(@PathVariable Long userId){
        Map<String, Object> authResponseBody = authService.getSpecificUser(userId);

        return ResponseEntity.ok(authResponseBody);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<NewRefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshRequest){

        NewRefreshTokenResponse response = authService.generateNewRefreshToken(refreshRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest logoutRequest){
        authService.logoutUser(logoutRequest);

        return ResponseEntity.ok("Logout Successfully");
    }


}
