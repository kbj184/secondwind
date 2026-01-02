package com.secondwind.config;

import com.secondwind.jwt.JWTFilter;
import com.secondwind.jwt.JWTUtil;
import com.secondwind.jwt.LoginFilter;
import com.secondwind.oauth2.CustomSuccessHandler;
import com.secondwind.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.lang.NonNull;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${secondwind.allowed-origins}")
        private String allowedOrigins;

        private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomSuccessHandler customSuccessHandler;
        private final JWTUtil jwtUtil;

        public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                        CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
                this.customOAuth2UserService = customOAuth2UserService;
                this.customSuccessHandler = customSuccessHandler;
                this.jwtUtil = jwtUtil;
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
                        throws Exception {

                /*
                 * http
                 * .requiresChannel(channel ->
                 * channel.anyRequest().requiresSecure()
                 * );
                 */

                http
                                .cors(corsCustomizer -> corsCustomizer
                                                .configurationSource(new CorsConfigurationSource() {

                                                        @Override
                                                        public CorsConfiguration getCorsConfiguration(
                                                                        @NonNull HttpServletRequest request) {

                                                                CorsConfiguration configuration = new CorsConfiguration();

                                                                if (allowedOrigins != null
                                                                                && !allowedOrigins.isEmpty()) {
                                                                        String[] origins = allowedOrigins.split(",");
                                                                        for (String origin : origins) {
                                                                                configuration.addAllowedOrigin(
                                                                                                origin.trim());
                                                                        }
                                                                }

                                                                configuration.setAllowedMethods(List.of("GET", "POST",
                                                                                "PUT", "DELETE", "OPTIONS", "PATCH"));
                                                                configuration.setAllowCredentials(true);
                                                                configuration.setAllowedHeaders(List.of("*"));
                                                                configuration.setMaxAge(3600L);
                                                                configuration.setExposedHeaders(List.of("Authorization",
                                                                                "Set-Cookie", "rt"));
                                                                // configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                                                                // configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                                                                // configuration.setExposedHeaders(Collections.singletonList("rt"));
                                                                // configuration.setExposedHeaders(
                                                                // List.of("__Host-at", "__Host-rt")
                                                                // );

                                                                return configuration;
                                                        }
                                                }));

                // csrf disable
                http.csrf(AbstractHttpConfigurer::disable);

                // From 로그인 방식 disable
                http.formLogin(AbstractHttpConfigurer::disable);

                // HTTP Basic 인증 방식 disable
                http.httpBasic(AbstractHttpConfigurer::disable);

                http
                                .addFilterAt(
                                                new LoginFilter(authenticationManager, customSuccessHandler),
                                                UsernamePasswordAuthenticationFilter.class);

                // JWTFilter 추가
                http.addFilterAfter(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
                // http.addFilterAfter(new JWTFilter(jwtUtil),
                // OAuth2LoginAuthenticationFilter.class);

                // http.addFilterAt(new
                // LoginFilter(authenticationManager(authenticationConfiguration),jwtUtil),
                // UsernamePasswordAuthenticationFilter.class);
                // oauth2
                http
                                .oauth2Login((oauth2) -> oauth2
                                                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                                                .userService(customOAuth2UserService))
                                                .successHandler(customSuccessHandler));

                // 경로별 인가 작업
                http
                                .authorizeHttpRequests((auth) -> auth
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                .requestMatchers("/", "/refresh/token", "/emailcheck", "/join",
                                                                "/login", "/favicon.ico", "/error")
                                                .permitAll()
                                                .anyRequest().authenticated());

                // 세션 설정 : STATELESS
                http
                                .sessionManagement((session) -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // Form Login 및 Http Basic 비활성화 (API 서버이므로)
                http
                                .formLogin((auth) -> auth.disable())
                                .httpBasic((auth) -> auth.disable());

                // 인증 실패 시 401 에러 반환 (HTML 페이지 리다이렉트 방지)
                http
                                .exceptionHandling((exceptions) -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.getWriter().write("Unauthorized");
                                                }));

                http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

                // http.addFilterBefore(new CustomLogoutFilter(jwtUtil), LogoutFilter.class);

                return http.build();
        }
}
