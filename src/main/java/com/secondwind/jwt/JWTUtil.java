package com.secondwind.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    public String getProviderId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("providerId",
                String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role",
                String.class);
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email",
                String.class);
    }

    public Boolean isExpired(String token) {

        try {
            Date exp = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return exp.before(new Date());

        } catch (ExpiredJwtException e) {
            return true;
        }

    }

    public String createJwt(Long id, String providerId, String email, String role, Long expiredMs) {
        System.out.println("createJwt==============createJwt");
        System.out.println(id);
        return Jwts.builder()
                .claim("id", id)
                .claim("providerId", providerId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category",
                String.class);
    }

    @Value("${secondwind.cookie-domain}")
    private String cookieDomain;

    public ResponseCookie createCookie(@NonNull String key, @NonNull String value) {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(key, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(60 * 60 * 60);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    /*
     * public Cookie createCookie(String key, String value) {
     * 
     * Cookie cookie = new Cookie(key, value);
     * cookie.setMaxAge(60*60*60);
     * cookie.setSecure(true);
     * cookie.setPath("/");
     * cookie.setHttpOnly(true);
     * 
     * return cookie;
     * }
     */

}