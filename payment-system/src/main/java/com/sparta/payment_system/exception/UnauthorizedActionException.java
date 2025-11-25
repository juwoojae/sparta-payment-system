package com.sparta.payment_system.exception;

/**
 * 권한이 부족해서 클라이언트의 요청을 수행하지 못할때 던지는 예외
 */
public class UnauthorizedActionException extends RuntimeException {

	public UnauthorizedActionException(String message) {
		super(message);
	}
}
