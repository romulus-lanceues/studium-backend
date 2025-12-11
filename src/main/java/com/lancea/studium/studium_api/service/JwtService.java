package com.lancea.studium.studium_api.service;

import com.lancea.studium.studium_api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${studium.token.key}")
    private String jwtSecret;

    @Value("${token.expiration.time}")
    private int jwtExpiration;

    //Use secure key derived from the secret
    private SecretKey key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    //========== TOKEN GENERATION ==========

    //Basic Approach
    public String generateJwtToken(User user){
            return Jwts.builder()
                    .subject(user.getEmail())
                    .claim("name", user.getFullName())
                    .claim("userId", user.getId())
                    .id(UUID.randomUUID().toString())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(key())
                    .compact();
    }



    //=========== TOKEN VALIDATION ==========


    //Validate token
    public boolean validateToken(String token, UserDetails userDetails){
        String email = getEmailFromToken(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    //Extracts a specific claim from the token
    public <T> T  extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = getClaimsFromToken(token); //Retrieve the claims from token
        return claimsResolver.apply(claims);
    }

    //Check if the token expiration
    public boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    //Validate and parse token
    public Claims getClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Extract email from token
    public String getEmailFromToken(String token){
        return extractClaim(token, Claims::getSubject);
    }

}

//    public String generateJwtToken(Authentication authentication){
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//
//        return Jwts.builder()
//                .setSubject()
//    }