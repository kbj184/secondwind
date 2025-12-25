package com.secondwind.controller;

import com.secondwind.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshTokenController {

    private final JWTUtil jwtUtil;

    public RefreshTokenController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/refresh/token")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("make new JWT0");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        for (Cookie cookie : cookies) {

            // if (cookie.getName().equals("__Host-rt")) {
            if (cookie.getName().equals("rt")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            System.out.println("make new JWT1");
            // response status code
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
            // return new ResponseEntity<>("refresh token null", HttpStatus.UNAUTHORIZED);
        }

        // expired check
        try {
            System.out.println("make new JWT2");
            if (jwtUtil.isExpired(refresh)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        // // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        // String category = jwtUtil.getCategory(refresh);
        //
        // if (!category.equals("refresh")) {
        //
        // //response status code
        // return ResponseEntity
        // .status(HttpStatus.UNAUTHORIZED)
        // .build();
        // //return new ResponseEntity<>("invalid refresh token",
        // HttpStatus.BAD_REQUEST);
        // }

        Long id = jwtUtil.getId(refresh);
        String poviderId = jwtUtil.getProviderId(refresh);
        String role = jwtUtil.getRole(refresh);
        String email = jwtUtil.getEmail(refresh); // Extract email
        System.out.println("make new JWT=" + id);
        System.out.println("make new JWT=" + poviderId);
        System.out.println("make new JWT=" + role);
        // make new JWT
        String newAccess = jwtUtil.createJwt(id, poviderId, email, role, 600000L);
        String newRefresh = jwtUtil.createJwt(id, poviderId, email, role, 86400000L);
        // response
        // response.setHeader("access", newAccess);
        response.addHeader("Authorization", "Bearer " + newAccess);
        // response.addCookie(jwtUtil.createCookie("__Host-rt", newRefresh));
        response.addHeader("Set-Cookie",
                // jwtUtil.createCookie("__Host-rt", newRefresh).toString());
                jwtUtil.createCookie("rt", newRefresh).toString());

        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}