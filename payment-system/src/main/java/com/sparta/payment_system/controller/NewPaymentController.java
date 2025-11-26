package com.sparta.payment_system.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.dto.payment.CancelPaymentRequest;
import com.sparta.payment_system.dto.payment.GetPaidPaymentListResponse;
import com.sparta.payment_system.dto.payment.VerifyPaymentRequest;
import com.sparta.payment_system.service.NewPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class NewPaymentController {
	private final NewPaymentService newPaymentService;

	// 결제 완료 검증
	@PostMapping("/complete")
	public Mono<ResponseEntity<String>> completePayment(@RequestBody VerifyPaymentRequest request) {
		log.info("엔드 포인트 호출 성공");
		Long orderId = request.getOrderId();
		String impUid = request.getImpUid();
		BigDecimal amount = request.getAmount();

		return newPaymentService.verifyPayment(impUid, orderId, amount)
			.doOnNext(result -> log.info("verifyPayment 결과: {}", result))
			.map(result -> {
				if (result)
					return ResponseEntity.ok("Payment Verified");
				else
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment Verification Failed");
			})
			.onErrorResume(e -> {
				log.error("결제 검증 중 예외 발생", e);
				return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()));
			});
	}

	// 결제 취소
	@PostMapping("/cancel")
	public Mono<ResponseEntity<String>> cancelPayment(@RequestBody CancelPaymentRequest cancelRequest) {
		String impUid = cancelRequest.getImpUid();
		String reason = cancelRequest.getReason() != null
			? cancelRequest.getReason()
			: "사용자에 의한 요청 취소";

		log.info("취소 요청: impUid={}, reason={}", impUid, reason);

		return newPaymentService.cancelPayment(impUid, reason)
			.map(result -> {
				if (result) {
					return ResponseEntity.ok("결제 취소 성공");
				} else {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 취소 실패");
				}
			});
	}

	// 결제 내역 조회
	@GetMapping("/paid")
	public ResponseEntity<List<GetPaidPaymentListResponse>> getPaidPaymentList() {
		List<GetPaidPaymentListResponse> paidList = newPaymentService.getPaidPaymentList();
		return ResponseEntity.status(HttpStatus.OK).body(paidList);
	}

}

