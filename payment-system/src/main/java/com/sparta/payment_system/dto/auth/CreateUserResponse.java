package com.sparta.payment_system.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateUserResponse {
    private Long userId;
    private String email;
    private String password;
    private String userName;
    private LocalDateTime createdAt;

    public CreateUserResponse(Long id, String email, String password, String name, LocalDateTime createdAt) {
        this.userId = id;
        this.email = email;
        this.password = password;
        this.userName = name;
        this.createdAt = createdAt;
    }
}
