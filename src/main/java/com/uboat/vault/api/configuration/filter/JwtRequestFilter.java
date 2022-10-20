package com.uboat.vault.api.configuration.filter;

import com.uboat.vault.api.business.services.JwtService;
import com.uboat.vault.api.business.services.JwtUserDetailsService;
import com.uboat.vault.api.model.exceptions.UBoatJwtException;
import com.uboat.vault.api.utilities.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Value("${whitelist}")
    private String[] whiteListUrls;

    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    public JwtRequestFilter(JwtService jwtService, JwtUserDetailsService jwtUserDetailsService) {
        this.jwtService = jwtService;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    private void handleNoAuthorizationRequest(HttpServletRequest request) {
        if (Arrays.stream(whiteListUrls).noneMatch(url -> request.getRequestURI().contains(url)))
            logger.warn(LoggingUtils.colorString("API requires authorization and header is empty.", LoggingUtils.TextColor.RED));
        else
            logger.info(LoggingUtils.colorString("Api does not require authorization.", LoggingUtils.TextColor.GREEN));
    }

    private void handleRTokenRequest() {
        logger.info(LoggingUtils.colorString("Authorization 'RToken ' header found.", LoggingUtils.TextColor.GREEN));
    }

    private void handleBearerRequest(String jwtToken, HttpServletRequest request) {
        logger.info(LoggingUtils.colorString("Authorization 'Bearer ' header found.", LoggingUtils.TextColor.GREEN));

        try {
            var jwtData = jwtService.extractUsernameAndPhoneNumber(jwtToken);
            logger.info(LoggingUtils.colorString("JWT is valid", LoggingUtils.TextColor.GREEN));

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = jwtUserDetailsService.loadUserByUsername(jwtData.join());

                if (jwtService.validateJsonWebToken(jwtToken)) {
                    var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        } catch (UsernameNotFoundException e) {
            logger.warn(LoggingUtils.colorString("Valid JWT token with username not bound to any account found.", LoggingUtils.TextColor.RED));
        } catch (UBoatJwtException e) {
            logger.warn(LoggingUtils.colorString("JWT is invalid: " + e.getStatus().getServerMessage(), LoggingUtils.TextColor.RED));
        }
    }

    private void handleNotRecognizedAuthorizationRequest() {
        logger.warn(LoggingUtils.colorString("Authorization header exists but does not begin with 'Bearer' or 'RToken'.", LoggingUtils.TextColor.RED));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
        final var header = request.getHeader("Authorization");

        if (header == null) handleNoAuthorizationRequest(request);
        else if (header.startsWith("RToken ")) handleRTokenRequest();
        else if (header.startsWith("Bearer ")) handleBearerRequest(header.substring(7), request);
        else handleNotRecognizedAuthorizationRequest();

        chain.doFilter(request, response);
    }
}