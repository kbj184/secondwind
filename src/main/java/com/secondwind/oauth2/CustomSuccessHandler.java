package com.secondwind.oauth2;

import com.secondwind.dto.CustomOAuth2User;
import com.secondwind.dto.CustomUserDetail;
import com.secondwind.jwt.AuthUser;
import com.secondwind.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${secondwind.allowed-origins}")
    private String allowedOrigins;

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        String providerId;
        Long id;
        if (principal instanceof CustomUserDetail user) {
            providerId = user.getProviderId(); // 일반 로그인
            id = user.getId();
        } else if (principal instanceof CustomOAuth2User oauth) {
            providerId = oauth.getProviderId(); // OAuth
            id = oauth.getId();
        } else {
            throw new IllegalStateException("Unknown principal type");
        }

        // OAuth2User
        // CustomOAuth2User customUserDetails = (CustomOAuth2User)
        // authentication.getPrincipal();
        // String providerId = customUserDetails.getProviderId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();
        String email;
        if (principal instanceof AuthUser user) {
            email = user.getEmail();
        } else {
            email = null;
        }

        System.out.println("22222222222id:" + id);
        // String token = jwtUtil.createJwt("access",username, role, 60*60*60L);
        // 토큰 생성
        // String access = jwtUtil.createJwt("access", username, role, 600000L);
        String refresh = jwtUtil.createJwt(id, providerId, email, role, 86400000L);
        System.out.println("22222222222id:" + refresh);
        // response.setHeader("access", access);
        // response.addCookie(createCookie("refresh", refresh));
        // response.setStatus(HttpStatus.OK.value());
        // response.addCookie(createCookie("Authorization", token));
        // response.addCookie(jwtUtil.createCookie("__Host-at", access));
        // response.addCookie(jwtUtil.createCookie("__Host-rt", refresh));
        response.addHeader("Set-Cookie",
                // jwtUtil.createCookie("__Host-rt", refresh).toString());
                jwtUtil.createCookie("rt", refresh != null ? refresh : "").toString());

        // ✅ OAuth만 redirect
        if (principal instanceof CustomOAuth2User) {
            String targetUrl = allowedOrigins.split(",")[0].trim();
            System.out.println("OAuth Login Success: Redirecting to " + targetUrl);
            response.sendRedirect(targetUrl);
        } else {
            System.out.println("Local Login Success");
            response.setStatus(HttpServletResponse.SC_OK);
        }

    }

}
