package com.lancea.studium.studium_api.security;

import com.lancea.studium.studium_api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{

        System.out.println("=== FILTER CALLED ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        //Retrieve the Auth header
        final String authHeader = request.getHeader("Authorization");

        //Check if the header contains something
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response); //Continue without authentication
            return;
        }

        //Extract the token and remove the "Bearer " prefix
        final String jwt = authHeader.substring(7);

        //Extract email from the token
        final String email = jwtService.getEmailFromToken(jwt);

        //Verify the email and if it's not already authenticated
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            //Load User Details from database for extra security
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            //Validate credentials
            if(jwtService.validateToken(jwt, userDetails)){

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

        filterChain.doFilter(request, response);
    }
}
