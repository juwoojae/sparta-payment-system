package com.sparta.payment_system.global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 로그인/회원가입은 JWT 검증 제외
        if (requestUri.startsWith("/user/register") ||
                requestUri.startsWith("/user/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 가져오기
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 토큰 없을시 인증 없이 다음 필터로 넘김
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 제거
        String token = authHeader.substring(7);

        try {
            // 토큰 검증
            Claims claims = jwtUtils.validateToken(token);

            Long userId = claims.get("userId", Long.class);
            String email = claims.get("email", String.class);
            String userName = claims.get("userName", String.class);

            // 인증 객체 생성
            UserInfo principal = new UserInfo(userId, email, userName);

            //SecurityContext 저장할 Authentication 객체 생성
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            // 인자 : 사용자 정보, 비밀번호, 권한목록
                            principal,
                            null, // JWT에서는 비밀번호 필요 없어서 null
                            Collections.emptyList() // 권한 목록 현재 없어서 빈리스트
                    );

            // SecurityContext에 인증 객체 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // JWT 에러시 인증 실패 처리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

