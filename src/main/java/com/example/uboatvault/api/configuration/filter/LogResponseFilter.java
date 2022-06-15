package com.example.uboatvault.api.configuration.filter;

import com.example.uboatvault.api.utilities.LoggingUtils;
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
public class LogResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

        // Execution request chain
        filterChain.doFilter(req, resp);

        // Get Cache
        byte[] responseBody = resp.getContentAsByteArray();

        LoggingUtils.logResponse(HttpMethod.valueOf(request.getMethod()), request.getRequestURI(), new String(responseBody, StandardCharsets.UTF_8));

        resp.copyBodyToResponse();
    }
}
