package com.sparta.payment_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.payment_system.client.PortOneClient;
import com.sparta.payment_system.dto.payment.GetPaidPaymentListResponse;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderStatus;
import com.sparta.payment_system.entity.Payment;
import com.sparta.payment_system.entity.PaymentStatus;
import com.sparta.payment_system.entity.Refund;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.PaymentRepository;
import com.sparta.payment_system.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class NewPaymentService {

	private final PortOneClient portOneClient;
	private final PaymentRepository paymentRepository;
	private final RefundRepository refundRepository;
	private final OrderRepository orderRepository;

	private final ProductService productService;
	private final PointService pointService;
	private final MemberShipService memberShipService;

	/**
	 * 결제 검증 및 DB 업데이트
	 */
	public Mono<Boolean> verifyPayment(String impUid, Long orderId, BigDecimal expectedAmount) {
		return portOneClient.getAccessToken()
			.flatMap(token -> portOneClient.getPaymentDetails(impUid, token))
			.flatMap(paymentDetails ->
				Mono.fromCallable(() -> processPayment(orderId, impUid, expectedAmount, paymentDetails))
					.subscribeOn(Schedulers.boundedElastic())
			)
			.onErrorReturn(false);
	}

	/**
	 * 실제 결제 검증 + DB 업데이트 처리 (블로킹)
	 */
	@Transactional
	public Boolean processPayment(Long orderId, String impUid, BigDecimal expectedAmount,
		Map<String, Object> paymentDetails) {

		System.out.println("paymentDetails = " + paymentDetails);
		// 1. 상태 검증
		String statusStr = (String)paymentDetails.get("status");
		System.out.println("statusStr = " + statusStr);

		PaymentStatus status = mapPortOneStatus(statusStr);

		System.out.println("status = " + status);
		if (status != PaymentStatus.PAID)
			return false;

		// 2. 금액 검증
		Map<String, Object> amountMap = (Map<String, Object>)paymentDetails.get("amount");

		System.out.println("amountMap = " + amountMap);

		BigDecimal paidAmount = extractTotalAmount(amountMap);
		System.out.println("paidAmount = " + paidAmount);
		System.out.println("expectedAmount = " + expectedAmount);
		if (paidAmount == null || paidAmount.compareTo(expectedAmount) != 0)
			return false;

		// 3. DB 업데이트
		updateOrderAndPayment(orderId, impUid, paidAmount, paymentDetails);

		return true;
	}

	/**
	 * 결제 취소
	 */
	@Transactional
	public Mono<Boolean> cancelPayment(String impUid, String reason) {
		return portOneClient.getAccessToken()
			.flatMap(token ->
				portOneClient.getPaymentDetails(impUid, token)
					.flatMap(paymentDetails -> {
						System.out.println("[CANCEL] paymentDetails = " + paymentDetails);

						String statusStr = (String)paymentDetails.get("status");
						PaymentStatus status = mapPortOneStatus(statusStr);
						System.out.println("[CANCEL] PG status = " + status);

						// 1) 아직 PAID 상태인 건만 취소 가능
						if (status != PaymentStatus.PAID) {
							System.out.println("[CANCEL] 이미 취소된 결제거나 취소 불가 상태입니다.");
							return Mono.just(false);
						}

						Map<String, Object> amountMap = (Map<String, Object>)paymentDetails.get("amount");
						BigDecimal cancelAmount = extractTotalAmount(amountMap);
						System.out.println("[CANCEL] cancelAmount = " + cancelAmount);

						// 2) PortOne 취소 API 호출
						return portOneClient.cancelPayment(impUid, token, reason)
							.flatMap(result -> {
								System.out.println("[CANCEL] PG 취소 응답 = " + result);

								// 3) 우리 DB 롤백 (블로킹 → boundedElastic)
								return Mono.fromCallable(() -> {
									handleCancelInDatabase(impUid, cancelAmount, reason);
									System.out.println("[CANCEL] 로컬 DB 롤백 완료");
									return true;
								}).subscribeOn(Schedulers.boundedElastic());
							});
					})
			)
			.onErrorResume(e -> {
				e.printStackTrace();
				return Mono.just(false);
			});
	}

	public List<GetPaidPaymentListResponse> getPaidPaymentList() {
		List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.PAID);

		return payments.stream()
			.map(p -> new GetPaidPaymentListResponse(
				p.getPaymentId(),
				p.getOrder().getOrderId(),
				p.getImpUid(),
				p.getAmount(),
				p.getPaymentMethod(),
				p.getStatus(),
				p.getStatus() == PaymentStatus.PAID
			))
			.toList();
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
			default -> PaymentStatus.FAILED;
		};
	}

	public BigDecimal extractTotalAmount(Map<?, ?> amountInfo) {
		if (amountInfo == null)
			return null;
		Object total = amountInfo.get("total");
		if (total instanceof Number)
			return BigDecimal.valueOf(((Number)total).doubleValue());
		if (total instanceof String s && !s.isBlank()) {
			try {
				return new BigDecimal(s);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	@Transactional
	public void updateOrderAndPayment(Long orderId, String impUid, BigDecimal paidAmount,
		Map<String, Object> paymentDetails) {
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

		Payment payment = paymentRepository.findByOrder(order)
			.orElseGet(() -> new Payment(order, paidAmount, impUid, PaymentStatus.PAID, null));

		// Object payMethodObj = paymentDetails.get("payMethod");
		// String method = payMethodObj instanceof String m ? m : null;

		Map<String, Object> methodMap = (Map<String, Object>)paymentDetails.get("method");
		String method = null;
		if (methodMap != null) {
			String provider = (String)methodMap.get("provider");
			method = provider;
		}

		System.out.println("method = " + method);

		payment.completePayment(paidAmount, method);
		paymentRepository.save(payment);

		updateAfterPayment(order, payment, paidAmount, paymentDetails);

		order.updateOrderStatus(OrderStatus.COMPLETED);
		orderRepository.save(order);

		System.out.println("결제/주문 상태 갱신 완료 - orderId=" + orderId + ", impUid=" + impUid);
	}

	public void updateAfterPayment(Order order, Payment payment, BigDecimal paidAmount,
		Map<String, Object> paymentDetails) {
		if (order == null || order.getOrderId() == null)
			return;
		Long orderId = order.getOrderId();

		productService.decreaseStockForOrder(orderId);
		memberShipService.updateMemberShipByOrder(orderId);
		pointService.earnPointsAfterPayment(order, paidAmount);
	}

	@Transactional
	public void handleCancelInDatabase(String impUid, BigDecimal cancelAmount, String reason) {
		Payment payment = paymentRepository.findByImpUid(impUid)
			.orElseThrow(() -> new IllegalArgumentException("해당 impUid 결제 정보를 찾을 수 없습니다. impUid=" + impUid));

		if (payment.getStatus() != PaymentStatus.PAID)
			return;

		// 1. 결제 상태 REFUNDED로 변경
		payment.updatePaymentStatus(PaymentStatus.REFUNDED);
		paymentRepository.save(payment);

		Order order = payment.getOrder();
		Long orderId = (order != null ? order.getOrderId() : null);

		if (orderId != null) {

			// 3) 재고/포인트/멤버십 롤백
			rollbackAfterCancel(orderId, cancelAmount);

			// 4) 주문은 Repository로 다시 조회해서 상태 변경
			Order managedOrder = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

			managedOrder.updateOrderStatus(OrderStatus.CANCELLED);
			orderRepository.save(managedOrder);

			// 환불 이력 기록
			Refund refund = new Refund(payment, cancelAmount, reason);
			refundRepository.save(refund);
		}

		System.out.println("결제 취소 후 DB 상태 갱신 완료 - impUid=" + impUid);
	}

	public void rollbackAfterCancel(Long orderId, BigDecimal cancelAmount) {
		productService.rollbackStockForOrder(orderId);
		pointService.rollbackPointsAfterCancel(orderId);
		memberShipService.updateMemberShipByOrder(orderId);
	}
}