package com.example.Flicktionary.global.security;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.global.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 쿠키로 JWT를 받아 유저를 인증하는 커스텀 인증 필터.
 * OncePerRequestFilter을 상속하므로, {@link SecurityConfig}의 엔드포인트 보호 설정을 무시한다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * URI 경로를 검증하는 AntPathMatcher 오브젝트.
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 인증에서 제회할 URI 경로.
     */
    private static final String[] excluded_urls = {
            "/api/users/login",
            "/api/users/register",
            "/api/users/refresh",
            "/h2-console/**"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String url = request.getRequestURI();
        return Stream.of(excluded_urls).anyMatch(path -> pathMatcher.match(path, url));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        // 쿠키가 비어있다면 인증을 거부한다.
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 접근 토큰이 없거나 빈 값을 가지고 있다면 인증을 거부한다.
        Cookie authCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("accessToken")).findFirst().orElse(null);
        if (authCookie == null || authCookie.getValue().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 접근 토큰에서 회원 정보를 꺼내 해당 회원을 현재 인증된 회원으로 지정한다.
            UserAccount requestUser = userAccountJwtAuthenticationService.retrieveUserFromAccessToken(authCookie.getValue());
            UserDetails customUserDetails = customUserDetailsService.loadUserByUsername(requestUser.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 접근 토큰이 유효하지 않을때 리프레시 토큰으로 재발급을 시도한다.
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
            response.setHeader("Location", URI.create("/api/users/refresh").toString());
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(ResponseDto.of(
                            HttpStatus.TEMPORARY_REDIRECT.value() + "",
                            "접근 토큰이 유효하지 않습니다."
                    )));
        }
    }
}
