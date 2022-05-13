package com.example.uboatvault.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class CookiesService {
    private final Logger log = LoggerFactory.getLogger(CookiesService.class);

    public void addTokenToSetCookiesHeader(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token",token);
        cookie.setMaxAge( 30 * 60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("Added token to Set-Cookie header.");
    }
}
