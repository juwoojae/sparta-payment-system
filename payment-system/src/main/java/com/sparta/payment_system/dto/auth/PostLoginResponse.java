package com.sparta.payment_system.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostLoginResponse {

	private Long userId;
	private String userName;
	private String accessToken;
	private String refreshToken;

	public PostLoginResponse(Long userId, String userName, String accessToken, String refreshToken) {

		this.userId = userId;
		this.userName = userName;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
}
