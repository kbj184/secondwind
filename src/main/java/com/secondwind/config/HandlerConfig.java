package com.secondwind.config;

import com.secondwind.jwt.JWTUtil;
import com.secondwind.oauth2.CustomSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfig {

    @Bean
    public CustomSuccessHandler customSuccessHandler(JWTUtil jwtUtil) {
        return new CustomSuccessHandler(jwtUtil);
    }
}