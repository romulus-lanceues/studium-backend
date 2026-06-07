package com.lancea.studium.studium_api.security;

import com.lancea.studium.studium_api.util.CookieUtil;
import com.lancea.studium.studium_api.exception.InvalidJwtTokenException;
import com.lancea.studium.studium_api.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, CookieUtil cookieUtil){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{

        System.out.println("=== FILTER CALLED ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        if(request.getRequestURI().contains("/api/v1/auth")){
            filterChain.doFilter(request, response);
            return;
        }

        //Retrieve cookies
        final String jwtToken = cookieUtil.getJwtFrom(request);

            try{
                //Extract email from the token
                final String email = jwtService.getEmailFromToken(jwtToken);

                //Verify the email and if it's not already authenticated
                if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
                    //Load User Details from database for extra security
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    //Validate credentials
                    if(jwtService.validateToken(jwtToken, userDetails)){

                        System.out.println("Token Validation Successful");
                        System.out.println(userDetails.getAuthorities());

                        //If passed create an authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, //User info
                                null, // credentials (not needed after authentication)
                                userDetails.getAuthorities()
                        );

                        //Set additional details
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        //Update Security Context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }

            } catch (ExpiredJwtException exception) {
                request.setAttribute("REQUEST_EXCEPTION", exception);
                throw exception;
            } catch (JwtException exception){
                request.setAttribute("REQUEST_EXCEPTION", exception);
                throw new InvalidJwtTokenException("Invalid");
            }


        filterChain.doFilter(request, response);

    }
}
