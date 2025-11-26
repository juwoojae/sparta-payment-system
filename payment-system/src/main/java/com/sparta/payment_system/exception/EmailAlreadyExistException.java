package com.sparta.payment_system.exception;

/**
 * 도메인 예외
 * 하나의 사용자가 중복된 이메일로 회원가입을 시도하는 경우
 */
public class EmailAlreadyExistException extends RuntimeException {
	public EmailAlreadyExistException(String message) {

		super(message);
	}
}
