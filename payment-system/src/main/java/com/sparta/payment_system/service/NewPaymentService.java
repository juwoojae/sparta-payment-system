package com.sparta.payment_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.payment_system.client.PortOneClient;
import com.sparta.payment_system.dto.payment.GetPaidPaymentListResponse;
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

	// 후처리 과정에 필요한 요소
	private final ProductService productService;
	private final PointService pointService;
	private final MemberShipService memberShipService;

	@Autowired
	public NewPaymentService(PortOneClient portOneClient, PaymentRepository paymentRepository,
		RefundRepository refundRepository, OrderRepository orderRepository, ProductService productService,
		PointService pointService, MemberShipService memberShipService) {
		this.portOneClient = portOneClient;
		this.paymentRepository = paymentRepository;
		this.refundRepository = refundRepository;
		this.orderRepository = orderRepository;
		this.productService = productService;
		this.pointService = pointService;
		this.memberShipService = memberShipService;

	}

	// 결제건 검증
	@Transactional
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
				// paymenDetails를 콘솔에 찍으면 PortOne에서 담고있는 결제 상세내역을 볼 수 있음
				updateOrderAndPayment(orderId, impUid, paidAmount, paymentDetails);

				return true;
			})
			.onErrorReturn(false);
	}

	// 결제 취소
	@Transactional
	public Mono<Boolean> cancelPayment(String impUid, String reason) {
		return portOneClient.getAccessToken()
			.flatMap(accessToken ->
				// 1) 결제 상세 조회
				portOneClient.getPaymentDetails(impUid, accessToken)
					.flatMap(paymentDetails -> {
						System.out.println("취소 대상 결제 정보: " + paymentDetails);

						// 2) 상태 확인
						// PortOne에서의 상태값은 문자열로 되어있으므로 Enum으로 전환
						String statusStr = (String)paymentDetails.get("status");
						PaymentStatus status = mapPortOneStatus(statusStr);

						if (status != PaymentStatus.PAID) {
							System.out.println("이미 취소되었거나 실패한 결제입니다. status=" + statusStr);
							//Mono<Boolean> 형식에 맞춰서 조건을 만족하지 않을 경우 false를 담은 Mono 형태로 보내줌
							return Mono.just(false);
						}

						// 3) 취소 금액 추출 (전액 취소)
						Map<String, Object> amountInfo = (Map<String, Object>)paymentDetails.get("amount");
						BigDecimal cancelAmount = extractTotalAmount(amountInfo);

						// 4) PortOne 결제 취소 API 호출
						return portOneClient.cancelPayment(impUid, accessToken, reason)
							.flatMap(cancelResult -> {
								// 5) 우리 DB 상태 갱신 (JPA라 블로킹이라서 fromCallable로 감싸줌)
								return Mono.fromCallable(() -> {
										handleCancelInDatabase(impUid, cancelAmount, reason);
										return true;
									})
									.onErrorReturn(false);
							});
					})
			)
			.onErrorReturn(false);
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
				p.getPaidAt(),
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
			default -> PaymentStatus.FAILED; // 알 수 없는 값은 실패로 처리
		};
	}

	// 결제 총 금액 추출
	private BigDecimal extractTotalAmount(Map<?, ?> amountInfo) {
		if (amountInfo == null)
			return null;

		Object total = amountInfo.get("total");
		// total이 Number의 서브타입(Double, Integer, Long 이면 if문에 들어감)
		if (total instanceof Number) {
			return BigDecimal.valueOf(((Number)total).doubleValue());
		}

		// Number 타입이 아닌 것들의 경우 조건문 설정, 문자열도 Decimal로 전환해보고 안되면 null 보내본다.
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
		// 1. 주문 조회
		Order order = orderRepository.findByOrderId(orderId)
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

		// 성공시 후처리 기능 호출
		updateAfterPayment(order, payment, paidAmount, paymentDetails);

		order.updateOrderStatus(OrderStatus.COMPLETED);
		orderRepository.save(order);

		System.out.println("결제/주문 상태 갱신 완료 - orderId=" + orderId + ", impUid=" + impUid);
	}

	// 결제 성공 후 후처리 (재고 차감, 포인트 적립, 누적금액 확인 후 등급 업데이트)

	private void updateAfterPayment(Order order,
		Payment payment,
		BigDecimal paidAmount,
		Map<String, Object> paymentDetails) {
		if (order == null || order.getOrderId() == null) {
			return;
		}

		Long orderId = order.getOrderId();

		// 1. 재고 차감: 주문에 포함된 상품 재고를 감소
		productService.decreaseStockForOrder(orderId);

		// 2. 포인트 적립: 결제 금액/주문 정보를 기준으로 포인트 적립
		pointService.earnPointsAfterPayment(order, paidAmount);

		// 3. 멤버십 등급 업데이트: 최근 90일 기준 누적 금액으로 등급 재계산
		memberShipService.updateMemberShipByOrder(orderId);
	}

	// 결제 취소 후 DB 수정 헬퍼 메서드
	private void handleCancelInDatabase(String impUid, BigDecimal cancelAmount, String reason) {
		// 1. impUid로 Payment 조회
		Payment payment = paymentRepository.findByImpUid(impUid)
			.orElseThrow(() -> new IllegalArgumentException("해당 impUid의 결제 정보를 찾을 수 없습니다. impUid=" + impUid));

		// 2. 이미 취소/환불된 결제라면 추가 처리 없이 리턴
		if (payment.getStatus() != PaymentStatus.PAID) {
			System.out.println("이미 취소되었거나 취소 불가능한 상태의 결제입니다. status=" + payment.getStatus());
			return;
		}

		// 3. 결제 상태를 REFUNDED 로 변경 (전액 취소 기준)
		payment.updatePaymentStatus(PaymentStatus.REFUNDED);
		paymentRepository.save(payment);

		// 취소시 후처리 기능 호출
		rollbackAfterCancel(payment, cancelAmount);

		// 4. 연관 주문 상태를 CANCELLED 로 변경
		Order order = payment.getOrder();
		if (order != null) {
			order.updateOrderStatus(OrderStatus.CANCELLED);
			orderRepository.save(order);
		}

		System.out.println("결제 취소 후 DB 상태 갱신 완료 - impUid=" + impUid + ", cancelAmount=" + cancelAmount);
	}

	// 결제 취소 후 후처리 (재고 원복, 포인트 원복, 멤버십 등급 재계산)
	private void rollbackAfterCancel(Payment payment, BigDecimal cancelAmount) {
		Order order = payment.getOrder();
		if (order == null || order.getOrderId() == null) {
			return;
		}

		Long orderId = order.getOrderId();

		// 1. 재고 원복
		productService.rollbackStockForOrder(orderId);

		// 2. 포인트 원복
		pointService.rollbackPointsAfterCancel(order);

		// 3. 멤버십 등급 재계산 (최근 90일 기준으로 다시 계산)
		memberShipService.updateMemberShipByOrder(orderId);
	}
}
