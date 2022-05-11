package com.example.uboatvault.api.services;

import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class CookiesService {

    public void addTokenToSetCookiesHeader(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token",token);
        cookie.setMaxAge( 30 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
