package com.sparta.payment_system.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostRefreshTokenResponse {

    private String accessToken;
    private String refreshToken;
}
