package com.sparta.payment_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공통 예외 응답 처리
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
	private String message;
}
