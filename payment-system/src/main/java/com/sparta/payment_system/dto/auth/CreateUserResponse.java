package com.sparta.payment_system.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateUserResponse {

    private Long userId;
    private String email;
    private String userName;
    private LocalDateTime createdAt;

    public CreateUserResponse(Long id, String email, String name, LocalDateTime createdAt) {

        this.userId = id;
        this.email = email;
        this.userName = name;
        this.createdAt = createdAt;
    }
}
