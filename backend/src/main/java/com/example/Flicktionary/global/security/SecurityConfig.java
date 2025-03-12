package com.example.Flicktionary.global.security;

import com.example.Flicktionary.global.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // JSON 직렬/역직렬화 유틸리티 클래스를 따로 구현할지 여부를 고려해볼것
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final CorsConfig corsConfig;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        /** 모든 엔드포인트를 개방한다. {@link CustomAuthenticationFilter} 참조. */
                        authorizeHttpRequests
                                .anyRequest()
                                .permitAll())
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfig))
                // CSRF 보호 비활성
                .csrf(AbstractHttpConfigurer::disable)
                // XSS 보호 비활성
                .headers(AbstractHttpConfigurer::disable)
                // Spring Security 세션 비활성 (JSESSIONID 쿠키 비생성)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 커스텀 인증 필터 추가
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 인증 필터 예외 처리
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");
                                            response.setStatus(HttpStatus.FORBIDDEN.value());
                                            response.getWriter().write(
                                                    objectMapper.writeValueAsString(ResponseDto.of(
                                                            HttpStatus.FORBIDDEN.value() + "",
                                                            "인증 토큰이 잘못되었거나 로그인이 필요합니다."
                                                    )));
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");
                                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                            response.getWriter().write(
                                                    objectMapper.writeValueAsString(ResponseDto.of(
                                                            HttpStatus.UNAUTHORIZED.value() + "",
                                                            "접근 권한이 없습니다."
                                                    )));
                                        }
                                )
                );
        return http.build();
    }
}
