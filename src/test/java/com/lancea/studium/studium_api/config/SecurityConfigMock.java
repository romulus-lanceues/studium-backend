package com.lancea.studium.studium_api.config;

import com.lancea.studium.studium_api.service.JwtService;
import com.lancea.studium.studium_api.util.CookieUtil;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(SecurityConfig.class)
public abstract class SecurityConfigMock {
    //Security config dependencies
    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private AuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private AccessDeniedHandler accessDeniedHandler;
}
