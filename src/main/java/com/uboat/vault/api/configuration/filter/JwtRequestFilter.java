package com.uboat.vault.api.configuration.filter;

import com.uboat.vault.api.services.JwtService;
import com.uboat.vault.api.services.JwtUserDetailsService;
import com.uboat.vault.api.utilities.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String jwtToken = null;
        String usernameAndPhoneNumber = null;
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
        } else {
            if (requestTokenHeader.startsWith("Bearer ")) {
                logger.info(LoggingUtils.colorString("Authorization Bearer header found.", LoggingUtils.TextColor.GREEN));
                jwtToken = requestTokenHeader.substring(7);
                usernameAndPhoneNumber = jwtService.extractUsernameAndPhoneNumber(jwtToken);
                logger.info(LoggingUtils.colorString("Credentials '" + usernameAndPhoneNumber.replace("null", "").replace("\t", "") + "' with token: " + jwtToken, LoggingUtils.TextColor.GREEN));
            } else if (requestTokenHeader.startsWith("RToken "))
                logger.info(LoggingUtils.colorString("Authorization RToken header found.", LoggingUtils.TextColor.GREEN));
            else
                logger.warn(LoggingUtils.colorString("JWT Token does not begin with Bearer String", LoggingUtils.TextColor.RED));
        }

        if (usernameAndPhoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = jwtUserDetailsService.loadUserByUsername(usernameAndPhoneNumber);
            if (jwtService.validateJsonWebToken(jwtToken)) {
                var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        chain.doFilter(request, response);
    }

}