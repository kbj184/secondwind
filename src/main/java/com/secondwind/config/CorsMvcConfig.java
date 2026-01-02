package com.secondwind.config;

import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Value("${secondwind.allowed-origins}")
    private String allowedOrigins;

    @Override
    @SuppressWarnings("null")
    public void addCorsMappings(@NonNull CorsRegistry corsRegistry) {
        if (allowedOrigins != null) {
            String[] origins = allowedOrigins.split(",");
            corsRegistry.addMapping("/**")
                    .exposedHeaders("Set-Cookie")
                    .allowedOrigins(origins);
        }
    }
}