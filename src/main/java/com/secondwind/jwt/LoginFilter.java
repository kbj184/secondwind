package com.secondwind.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler successHandler) {
        this.authenticationManager = authenticationManager;
        // setUsernameParameter("email");
        // setPasswordParameter("password");
        setAuthenticationSuccessHandler(successHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> loginData = objectMapper.readValue(request.getInputStream(),
                    new TypeReference<Map<String, String>>() {
                    });

            String email = loginData.get("email");
            String password = loginData.get("password");

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);

            return authenticationManager.authenticate(token);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // String email = obtainUsername(request);
        // String password = obtainPassword(request);
        // UsernamePasswordAuthenticationToken token = new
        // UsernamePasswordAuthenticationToken(email, password);
        // return authenticationManager.authenticate(token);
    }

    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        response.setStatus(401);
    }

}