package com.lancea.studium.studium_api.service;

import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final JwtService jwtService;

    public AdminService(JwtService jwtService){
        this.jwtService = jwtService;
    }

    public void revokeRefreshToken(String refreshToken){
        jwtService.revokeRefreshToken(refreshToken);
    }
}
