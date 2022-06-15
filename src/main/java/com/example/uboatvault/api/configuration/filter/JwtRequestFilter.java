package com.example.uboatvault.api.configuration.filter;

import com.example.uboatvault.api.services.JwtService;
import com.example.uboatvault.api.services.JwtUserDetailsService;
import com.example.uboatvault.api.utilities.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    public JwtRequestFilter(JwtService jwtService, JwtUserDetailsService jwtUserDetailsService) {
        this.jwtService = jwtService;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String jwtToken = null;
        String username = null;
        if (requestTokenHeader == null) {
            if (!List.of("/api/isVaultActive",
                    "/api/checkUsername",
                    "/api/checkPhoneNumber",
                    "/api/checkDeviceRegistration",
                    "/api/verifyJsonWebToken",
                    "/api/requestRegistration",
                    "/api/register",
                    "/api/login").contains(request.getRequestURI()))
                logger.warn(LoggingUtils.colorString("Authorization header is empty.", LoggingUtils.TextColor.RED));
            else
                logger.info(LoggingUtils.colorString("Api does not require authorization.", LoggingUtils.TextColor.GREEN));
        } else if (!requestTokenHeader.startsWith("Bearer "))
            logger.warn(LoggingUtils.colorString("JWT Token does not begin with Bearer String", LoggingUtils.TextColor.RED));
        else {
            if (requestTokenHeader.startsWith("RToken "))
                logger.info(LoggingUtils.colorString("Authorization RToken header found.", LoggingUtils.TextColor.PURPLE));
            else
                logger.info(LoggingUtils.colorString("Authorization Bearer header found.", LoggingUtils.TextColor.PURPLE));

            jwtToken = requestTokenHeader.substring(7);
            username = jwtService.extractUsername(jwtToken);
            logger.info(LoggingUtils.colorString("Username '" + username + "' has token: " + jwtToken, LoggingUtils.TextColor.GREEN));
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = jwtUserDetailsService.loadUserByUsername(username);
            if (jwtService.validateJsonWebToken(jwtToken)) {
                var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        chain.doFilter(request, response);
    }

}