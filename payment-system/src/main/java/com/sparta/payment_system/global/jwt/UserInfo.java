package com.sparta.payment_system.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfo {
    // JWT에 담긴 사용자 정보만을 사용해서 인증하기 위해
    // 커스텀 pricipal dto 생성
    private Long userId;
    private String email;
    private String userName;
}
