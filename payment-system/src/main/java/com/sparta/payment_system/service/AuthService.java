package com.sparta.payment_system.service;

import com.sparta.payment_system.dto.auth.*;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.global.jwt.JwtUtils;
import com.sparta.payment_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // 회원가입
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        if(userRepository.existsByEmail(createUserRequest.getEmail())){
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new  User(
                createUserRequest.getEmail(),
                createUserRequest.getPassword(),
                createUserRequest.getUserName()
        );

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        userRepository.save(user);

        return new CreateUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getCreatedAt()
        );
    }

    // 로그인
    @Transactional
    public PostLoginResponse userLogin(PostLoginRequest postLoginRequest) {

        User user = userRepository.findByEmail(postLoginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if(!passwordEncoder.matches(postLoginRequest.getPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtils.createAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getName()
        );

        String refreshToken = jwtUtils.createRefreshToken(user.getUserId());

        // refreshToken 로그인마다 갱신
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new PostLoginResponse(
                user.getUserId(),
                user.getName(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public void userLogout(PostLogoutRequest postLogoutRequest) {
        String refreshToken = postLogoutRequest.getRefreshToken();

        if (!jwtUtils.validateToken(refreshToken).isEmpty()) {
            return;
        }

        Long userId = jwtUtils.getUserIdFromRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에서 refreshToken 제거
        user.removeRefreshToken();
    }

    @Transactional
    public PostRefreshTokenResponse refreshToken(PostRefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        if (jwtUtils.validateToken(refreshToken).isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtUtils.getUserIdFromRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 비어있거나(로그아웃) DB에 저장된 토큰과 비교
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("이미 로그아웃 되었거나 유효하지 않은 토큰입니다.");
        }

        String newAccessToken = jwtUtils.createAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getName()
        );

        String newRefreshToken = jwtUtils.createRefreshToken(user.getUserId());

        return new PostRefreshTokenResponse(newAccessToken, newRefreshToken);
    }
}
