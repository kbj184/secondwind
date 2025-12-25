package com.secondwind.jwt;

import com.secondwind.dto.CustomOAuth2User;
import com.secondwind.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("================doFilterInternal doFilterInternal doFilterInternal===================");
        // cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        // String authorization_access = null;
        // Cookie[] cookies = request.getCookies();
        // for (Cookie cookie : cookies) {
        //
        // System.out.println(cookie.getName());
        // if (cookie.getName().equals("__Host-at")) {
        //
        // authorization_access = cookie.getValue();
        // }
        // }
        //
        // //Authorization 헤더 검증
        // if (authorization_access == null) {
        //
        // System.out.println("token null");
        // filterChain.doFilter(request, response);
        // //조건이 해당되면 메소드 종료 (필수)
        // return;
        // }
        //
        // //토큰
        // String token = authorization_access;
        //
        // //토큰 소멸 시간 검증
        // if (jwtUtil.isExpired(token)) {
        //
        // System.out.println("token expired");
        // //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // filterChain.doFilter(request, response);
        //
        // //조건이 해당되면 메소드 종료 (필수)
        // return;
        // }

        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null or invalid header: " + authorization);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("authorization header found: " + authorization);
        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];
        System.out.println("extracted token: " + token);

        // 토큰 소멸 시간 검증
        try {
            if (jwtUtil.isExpired(token)) {
                System.out.println("token expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token Expired");
                return; // 필터 체인 중단
            }
        } catch (Exception e) {
            System.out.println("token validation error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token Invalid");
            return;
        }

        // 토큰에서 username과 role 획득
        Long id = jwtUtil.getId(token);
        String providerId = jwtUtil.getProviderId(token);
        String role = jwtUtil.getRole(token);
        String email = jwtUtil.getEmail(token);

        // userDTO를 생성하여 값 set
        UserDTO userDTO = new UserDTO();
        userDTO.setId(id);
        userDTO.setProviderId(providerId);
        userDTO.setRole(role);
        userDTO.setEmail(email);
        userDTO.setName(email); // Use email as name for SecurityContext if name is not available

        // UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null,
                customOAuth2User.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Authentication authToken =
        // new UsernamePasswordAuthenticationToken(
        // id,
        // null,
        // List.of(new SimpleGrantedAuthority(role))
        // );

        // SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}