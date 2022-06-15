package com.example.uboatvault.api.configuration.filter;

import com.example.uboatvault.api.utilities.LoggingUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class LogRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        req.getParameterMap();
        byte[] requestBody = req.getContentAsByteArray();
        LoggingUtils.logRequest(HttpMethod.valueOf(request.getMethod()), request.getRequestURI(), new String(requestBody, StandardCharsets.UTF_8));
        return true;
    }
}