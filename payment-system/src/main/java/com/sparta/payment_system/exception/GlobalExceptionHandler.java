package com.sparta.payment_system.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sparta.payment_system.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "GlobalExceptionHandler")
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * . @Valid 유효성 검사 실패 시 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
	}


	/**
	 * 존재하지않는 엔티티 조회
	 */
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRuntime(NotFoundException ex) {

		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(NOT_FOUND).body(errorResponse);
	}

	/**
	 * 도메인 정책 위반 - 회원 등록에서 하나의 이메일을 중복등록 하는경우
	 */
	@ExceptionHandler (EmailAlreadyExistException.class)
	public ResponseEntity<ErrorResponse> handleUserDomainErrors(EmailAlreadyExistException ex) {

		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(CONFLICT).body(errorResponse);
	}

	/**
	 * 클라이언트의 요청을 실행할 권한이 부족한 경우
	 */
	@ExceptionHandler (UnauthorizedActionException.class)
	public ResponseEntity<ErrorResponse> handleAuthorizationErrors(UnauthorizedActionException ex) {

		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(UNAUTHORIZED).body(errorResponse);
	}

	/**
	 * 로그인 시 패스워드가 틀린경우
	 */
	@ExceptionHandler (InvalidatePasswordException.class)
	public ResponseEntity<ErrorResponse> handleLoginErrors(InvalidatePasswordException ex) {

		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
		return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
	}

    /**
     * 도메인 정책 위반 - 주문수량은 재고수량을 넘어갈수 없음
     */
    @ExceptionHandler (InvalidProductQuantityException.class)
    public ResponseEntity<ErrorResponse> handleOrderDomainErrors(InvalidProductQuantityException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(errorResponse);
    }
    
}