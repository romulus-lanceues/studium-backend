package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.dto.request.RefreshTokenRequest;
import com.lancea.studium.studium_api.service.AdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/admin-qwerty", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/revoke/token")
    public ResponseEntity<?> revokeRefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest){

        adminService.revokeRefreshToken(refreshTokenRequest.refreshToken());

        return ResponseEntity.ok("Token Revoked");
    }

}
