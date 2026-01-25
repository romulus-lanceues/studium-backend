package com.lancea.studium.studium_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lancea.studium.studium_api.dto.request.LoginRequest;
import com.lancea.studium.studium_api.dto.request.RegisterRequest;
import com.lancea.studium.studium_api.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthService authService;

    @Test
    void shouldReturnASuccessfulLogInProcess() throws Exception{

        LoginRequest loginRequest = new LoginRequest("samplemail@mail.com", "wonderfulPassword");



        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);

            //Simulate JWT-Token cookie
            Cookie jwtCookie = new Cookie("JWT-TOKEN", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ965678qw7yewgcyg");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600); // 1 hour
            jwtCookie.setAttribute("SameSite", "Strict");
            response.addCookie(jwtCookie);

            Cookie refreshCookie = new Cookie("REFRESH-TOKEN", "refresh-token-value-here");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800); // 7 days
            refreshCookie.setAttribute("SameSite", "Strict");
            response.addCookie(refreshCookie);

            return null;
        }).when(authService).verifyCredentials(any(LoginRequest.class), any(HttpServletResponse.class));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login Successful"))
                .andExpect(cookie().exists("JWT-TOKEN"))
                .andExpect(cookie().exists("REFRESH-TOKEN"))
                .andExpect(cookie().httpOnly("JWT-TOKEN", true))
                .andExpect(cookie().httpOnly("REFRESH-TOKEN", true))
                .andExpect(cookie().secure("JWT-TOKEN", true))
                .andExpect(cookie().secure("REFRESH-TOKEN", true))
                .andExpect(cookie().path("JWT-TOKEN", "/"))
                .andExpect(cookie().path("REFRESH-TOKEN", "/"))
                .andExpect(cookie().maxAge("JWT-TOKEN", 3600))
                .andExpect(cookie().maxAge("REFRESH-TOKEN", 604800))
                .andReturn();


        //Additional verification of cookie values
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).hasSize(2);

        Cookie jwtCookie = findCookie(cookies, "JWT-TOKEN");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isNotEmpty();

        Cookie refreshCookie = findCookie(cookies, "REFRESH-TOKEN");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isNotEmpty();

        //Verify the service was called exactly once
        verify(authService, times(1)).verifyCredentials(any(LoginRequest.class), any(HttpServletResponse.class));

    }

    @Test
    void shouldFailDueToInvalidEmailFormat() throws  Exception{
        LoginRequest loginRequest = new LoginRequest("ahdahaadashd", "wonderfulPassword");

         mockMvc.perform(post("/api/v1/auth//login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

         verify(authService, never()).verifyCredentials(any(LoginRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void shouldReturnSuccessfulRegistrationProcess() throws Exception{
        //Setup needed info
        RegisterRequest registerRequest = new RegisterRequest("themail@mail.com", "652ufnshwwiqd", "DaMan");

        //Create the mock logic for the authService
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);

            //Simulate JWT-Token cookie
            Cookie jwtCookie = new Cookie("JWT-TOKEN", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ965678qw7yewgcyg");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600); // 1 hour
            jwtCookie.setAttribute("SameSite", "Strict");
            response.addCookie(jwtCookie);

            Cookie refreshCookie = new Cookie("REFRESH-TOKEN", "refresh-token-value-here");
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(604800); // 7 days
            refreshCookie.setAttribute("SameSite", "Strict");
            response.addCookie(refreshCookie);

            return 3L;

        }).when(authService).createUser(any(RegisterRequest.class), any(HttpServletResponse.class));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(cookie().exists("JWT-TOKEN"))
                .andExpect(cookie().exists("REFRESH-TOKEN"))
                .andExpect(cookie().httpOnly("JWT-TOKEN", true))
                .andExpect(cookie().httpOnly("REFRESH-TOKEN", true))
                .andExpect(cookie().secure("JWT-TOKEN", true))
                .andExpect(cookie().secure("REFRESH-TOKEN", true))
                .andExpect(cookie().path("JWT-TOKEN", "/"))
                .andExpect(cookie().path("REFRESH-TOKEN", "/"))
                .andExpect(cookie().maxAge("JWT-TOKEN", 3600))
                .andExpect(cookie().maxAge("REFRESH-TOKEN", 604800))
                .andReturn();

        verify(authService, times(1)).createUser(any(RegisterRequest.class), any(HttpServletResponse.class));
    }


    // Helper method to find a cookie by name
    private Cookie findCookie(Cookie[] cookies, String name) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

}
