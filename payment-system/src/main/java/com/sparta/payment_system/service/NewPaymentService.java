package com.sparta.payment_system.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sparta.payment_system.client.PortOneClient;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderStatus;
import com.sparta.payment_system.entity.Payment;
import com.sparta.payment_system.entity.PaymentStatus;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.PaymentRepository;
import com.sparta.payment_system.repository.RefundRepository;

import reactor.core.publisher.Mono;

@Service
public class NewPaymentService {

	private final PortOneClient portOneClient;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final OrderRepository orderRepository;

	@Autowired
	public NewPaymentService(PortOneClient portOneClient, PaymentRepository paymentRepository,
		RefundRepository refundRepository, OrderRepository orderRepository) {
		this.portOneClient = portOneClient;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.orderRepository = orderRepository;
	}

	public Mono<Boolean> verifyPayment(String impUid, Long orderId, BigDecimal expectedAmount) {
		return portOneClient.getAccessToken()
			.flatMap(accessToken -> portOneClient.getPaymentDetails(impUid, accessToken))
			.map(paymentDetails -> {
				// paymentDetails는 PortOneClient가 반환한 JSON Map
				System.out.println("결제 정보 조회 결과: " + paymentDetails);

				// 1. 상태 검증 (포트원 status 문자열 → 우리 enum으로 매핑)
				String statusStr = (String)paymentDetails.get("status");
				PaymentStatus status = mapPortOneStatus(statusStr);

				if (status != PaymentStatus.PAID) {
					System.out.println("결제 상태 오류: " + statusStr);
					return false;
				}

				// 2. 금액 검증 (amount.total 기준)
				Map<String, Object> amountInfo = (Map<String, Object>)paymentDetails.get("amount");
				BigDecimal paidAmount = extractTotalAmount(amountInfo);

				if (paidAmount == null || paidAmount.compareTo(expectedAmount) != 0) {
					System.out.println("결제 금액 불일치: expected=" + expectedAmount + ", paid=" + paidAmount);
					return false;
				}

				// 3. 검증 통과 → 우리 DB(Order/Payment) 상태 갱신
				updateOrderAndPayment(orderId, impUid, paidAmount, paymentDetails);

				return true;
			})
			.onErrorReturn(false);
	}

	// ========== 헬퍼 메서드 ===========
	// portOne의 결제 상태를 Enum과 매칭
	private PaymentStatus mapPortOneStatus(String rawStatus) {
		if (rawStatus == null) {
			return PaymentStatus.FAILED;
		}

		return switch (rawStatus.toUpperCase()) {
			case "PAID" -> PaymentStatus.PAID;
			case "CANCELLED", "CANCELED" -> PaymentStatus.REFUNDED;
			case "FAILED" -> PaymentStatus.FAILED;
			default -> PaymentStatus.FAILED; // 알 수 없는 값은 실패로 처리
		};
	}

	// 결제 총 금액 추출
	private BigDecimal extractTotalAmount(Map<?, ?> amountInfo) {
		if (amountInfo == null)
			return null;

		Object total = amountInfo.get("total");
		if (total instanceof Number) {
			return BigDecimal.valueOf(((Number)total).doubleValue());
		}
		if (total instanceof String s && !s.isBlank()) {
			try {
				return new BigDecimal(s);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	// 결제 검증 통과 후 Order / Payment 갱신
	private void updateOrderAndPayment(Long orderId,
		String impUid,
		BigDecimal paidAmount,
		Map<String, Object> paymentDetails) {
		try {
			// 1. 주문 조회
			Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

			// 2. 해당 주문에 대한 Payment 있는지 조회 (1:1 구조라고 가정)
			Payment payment = paymentRepository.findByOrder(order)
				.orElseGet(() -> new Payment(
					order,
					paidAmount,
					impUid,
					PaymentStatus.PAID,
					null // 일단 null, 아래 completePayment에서 채움
				));

			// 2-1. 결제 수단 추출 (환경 따라 key가 payMethod / method 등일 수 있음)
			Object payMethodObj = paymentDetails.get("pay_method");
			String method = null;
			if (payMethodObj instanceof String m) {
				method = m;
			}

			// 2-2. 결제 완료 처리 (Entity 메서드 사용 -> 외부 검증에 문제가 없었으므로 내부 상태를 PAID로 설정)
			payment.completePayment(paidAmount, method);

			paymentRepository.save(payment);

			// 3. 주문 상태 갱신
			order.setStatus(OrderStatus.COMPLETED);
			orderRepository.save(order);

			System.out.println("결제/주문 상태 갱신 완료 - orderId=" + orderId + ", impUid=" + impUid);
		} catch (Exception e) {
			System.err.println("결제/주문 상태 갱신 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
