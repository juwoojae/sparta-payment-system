package com.sparta.payment_system.exception;

/**
 * 도메인 예외
 * 로그인시 패스워드가 일치하지 않을때
 */
public class InvalidatePasswordException extends RuntimeException {

	public InvalidatePasswordException(String message) {
		super(message);
	}
}