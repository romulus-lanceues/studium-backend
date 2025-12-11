package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.entity.User;
import com.lancea.studium.studium_api.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    private final AuthService authService;

    public TestController(AuthService authService){
        this.authService =authService;
    }

    //Token Authentication Test
    @PostMapping("/get-user/{email}")
    public String getUserDetails(@PathVariable String email){
        User userInfo = authService.getUserInfo(email);
        return "Hello" + userInfo.getFullName();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/secret-stuff")
    public String adminMessage(){
        return "Hello Admin!!!";
    }

    @GetMapping("/role-check")
    public String checkRole(Authentication authentication){ //Passed in automatically courtesy of Spring Security
        return "User Authorities: " + authentication.getAuthorities();
    }


}
