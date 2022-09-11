package com.uboat.vault.api.configuration.config;

import com.uboat.vault.api.configuration.filter.LogRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LogConfig implements WebMvcConfigurer {

    private final LogRequestInterceptor requestInterceptor;

    @Autowired
    public LogConfig(LogRequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**");
    }
}