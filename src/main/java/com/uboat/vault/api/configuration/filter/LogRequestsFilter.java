package com.uboat.vault.api.configuration.filter;

import com.uboat.vault.api.utilities.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@WebFilter(urlPatterns = "/*")
@Order(-999)
@Slf4j
public class LogRequestsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var req = new ContentCachingRequestWrapper(request);
        var res = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(req, res);
        } finally {
            String requestBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);

            res.copyBodyToResponse();

            LoggingUtils.logRequest(HttpMethod.valueOf(request.getMethod()), response.getStatus(), request.getRequestURI(), request.getQueryString(), requestBody,responseBody);
        }
    }
}
