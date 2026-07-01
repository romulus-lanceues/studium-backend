package com.lancea.studium.studium_api.controller;

import com.lancea.studium.studium_api.config.SecurityConfig;
import com.lancea.studium.studium_api.config.SecurityConfigMock;
import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.service.AuthService;
import com.lancea.studium.studium_api.service.JwtService;
import com.lancea.studium.studium_api.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Testing the auth controllers functionality and the security filter
 */

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends SecurityConfigMock {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;


    @Test
    void authenticateUser_Test() throws Exception{

        String loginRequest = """
                { "email": "testfeatures@email.com",
                "password": "password123"}
                """;

        doNothing().when(authService)
                .verifyCredentials(any(LoginRequest.class),
                        any(HttpServletResponse.class), any(String.class));

        mockMvc.perform(post("/api/v1/auth/login").
                contentType(MediaType.APPLICATION_JSON).
                header("User-Agent", "Test-device").
                content(loginRequest)).andExpect(status().isOk());
    }

    @Test
    void signUp_Test() throws Exception{

        String registerRequest = """
                { "email": "testemail@email.com",
                "password":"1234567889",
                "fullName":"admin"}
                """;

        when(authService.createUser(
                any(RegisterRequest.class), any(HttpServletResponse.class),
                any(String.class))).thenReturn(1L);

        mockMvc.perform(post("/api/v1/auth/register").
                contentType(MediaType.APPLICATION_JSON).
                header("User-Agent", "Test-Device").
                content(registerRequest)).andExpect(status().isCreated());
    }

    @Test
    void signUpBlankInput_Test() throws Exception{
        String registerRequest = """
                { "email": "",
                "password":"1234567889",
                "fullName":"admin"}
                """;

        when(authService.createUser(
                any(RegisterRequest.class), any(HttpServletResponse.class),
                any(String.class))).thenReturn(1L);

        mockMvc.perform(post("/api/v1/auth/register").
                contentType(MediaType.APPLICATION_JSON).
                header("User-Agent", "Test-Device").
                content(registerRequest)).andExpect(status().isBadRequest());

    }


    @Test
    void refreshToken_Test() throws Exception {

        doNothing().when(authService).generateNewRefreshToken(any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(String.class));

        mockMvc.perform(post("/api/v1/auth/refresh-token").
                contentType(MediaType.APPLICATION_JSON).
                header("User-Agent", "Test-device"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_Test() throws Exception{

        doNothing().when(authService).logoutUser(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/v1/auth/logout").
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
