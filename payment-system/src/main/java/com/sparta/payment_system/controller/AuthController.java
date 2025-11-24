package com.sparta.payment_system.controller;

import com.sparta.payment_system.dto.auth.*;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> registerUser(@RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(createUserRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<PostLoginResponse> loginUser(@RequestBody PostLoginRequest postLoginRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.userLogin(postLoginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@RequestBody PostLogoutRequest postLogoutRequest) {
        authService.userLogout(postLogoutRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<PostRefreshTokenResponse> refresh(@RequestBody PostRefreshTokenRequest request) {
        PostRefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.refreshToken(request));
    }
}
