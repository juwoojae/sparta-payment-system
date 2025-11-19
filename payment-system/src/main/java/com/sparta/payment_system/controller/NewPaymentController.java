package com.sparta.payment_system.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.dto.payment.PaymentCompleteRequest;
import com.sparta.payment_system.service.NewPaymentService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class NewPaymentController {
	private final NewPaymentService newPaymentService;

	// 결제 완료 검증
	@PostMapping("/complete")
	public Mono<ResponseEntity<String>> completePayment(
		@RequestBody PaymentCompleteRequest request
	) {
		Long orderId = request.getOrderId();
		String impUid = request.getImpUid();
		BigDecimal amount = request.getAmount();

		return newPaymentService.verifyPayment(impUid, orderId, amount)
			.map(isSuccess -> {
				if (isSuccess) {
					return ResponseEntity
						.status(HttpStatus.OK)
						.body("결제 검증 및 완료 처리 성공");
				} else {
					return ResponseEntity
						.status(HttpStatus.BAD_REQUEST)
						.body("결제 검증 실패");
				}
			});
	}
}

