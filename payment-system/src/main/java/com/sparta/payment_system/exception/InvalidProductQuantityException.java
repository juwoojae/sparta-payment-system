package com.sparta.payment_system.exception;

/**
 * 도메인 예외 - Product 의 수량을 0 이하로 수정하려고 할때 던져지는 예외
 */
public class InvalidProductQuantityException  extends RuntimeException {
    public InvalidProductQuantityException (String message) {
        super(message);
    }
}
