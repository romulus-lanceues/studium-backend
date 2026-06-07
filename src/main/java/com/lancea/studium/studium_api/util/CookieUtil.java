package com.lancea.studium.studium_api.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.cookie.max-age}")
    private int jwtMaxAge;

    @Value("${jwt.refresh.cookie.name}")
    private String refreshCookieName;

    @Value("${jwt.refresh.cookie.max-age}")
    private int refreshMaxAge;

    @Value("${jwt.cookie.secure}")
    private boolean secure;

    @Value("${jwt.cookie.domain}")
    private String domain;

    //========== ADD COOKIES ==========

    //Add JWT token to HTTPOnly cookie
    public void addJwtCookie(HttpServletResponse response, String token){
        Cookie cookie = createCookie(jwtCookieName, token, jwtMaxAge);
        response.addCookie(cookie);
    }

    //Add Refresh token to HTTPOnly cookie
    public void addRefreshTokenCookie(HttpServletResponse response, String token){
        Cookie refreshCookie = createCookie(refreshCookieName, token, refreshMaxAge);
        response.addCookie(refreshCookie);
    }

    //Add both at the same time
    public void addAuthCookies(HttpServletResponse response, String jwtToken, String refreshToken){
        addJwtCookie(response, jwtToken );
        addRefreshTokenCookie(response, refreshToken);
    }

    //========== GET TOKEN FROM COOKIES ==========

    public String getJwtFrom(HttpServletRequest request){
        return getCookieValue(request, jwtCookieName);
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request){
        return getCookieValue(request, refreshCookieName);
    }

    //========== DELETE COOKIE ==========

    public void deleteJwtTokenFromCookie(HttpServletResponse response){
        deleteCookie(response, jwtCookieName);
    }

    public void deleteRefreshTokenFromCookie(HttpServletResponse response){
        deleteCookie(response, refreshCookieName);
    }

    public void deleteBothCookies(HttpServletResponse response){
        deleteJwtTokenFromCookie(response);
        deleteRefreshTokenFromCookie(response);
    }



    //===========Private helper methods============

    private Cookie createCookie(String name, String value, int maxAge){
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure); //??? Production only??
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");

        if(!domain.isEmpty()){
            cookie.setDomain(domain);
        }

        return cookie;
    }

    private String getCookieValue(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void deleteCookie(HttpServletResponse response, String cookieName){
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        if(!domain.isEmpty()){
            cookie.setDomain(domain);
        }

        response.addCookie(cookie);
    }

}
