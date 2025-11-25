package com.sparta.payment_system.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostRefreshTokenRequest {
    private String refreshToken;
}
