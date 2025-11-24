package com.sparta.payment_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.sparta.payment_system.entity.Grade;
import com.sparta.payment_system.entity.MemberShip;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.Payment;
import com.sparta.payment_system.entity.PaymentStatus;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.repository.MemberShipRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.PaymentRepository;

@Service
public class MemberShipService {

	private final OrderRepository orderRepository;
	private final MemberShipRepository memberShipRepository;
	private final PaymentRepository paymentRepository;

	public MemberShipService(OrderRepository orderRepository, MemberShipRepository memberShipRepository,
		PaymentRepository paymentRepository) {
		this.orderRepository = orderRepository;
		this.memberShipRepository = memberShipRepository;
		this.paymentRepository = paymentRepository;
	}

	public void updateMemberShipByOrder(Long orderId) {
		// 1. 주문 조회
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
		User user = order.getUser();

		// 2. 이 유저의 PAID 주문들을 모두 조회
		List<Payment> paidPayments =
			paymentRepository.findAllByOrder_UserAndStatus(user, PaymentStatus.PAID);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime threshold = now.minusDays(90); //조회일로부터 90일 내 금액을 조회하기 위한 기준일 설정
		// 3. 최근 90일 기준 누적 결제금액 계산
		BigDecimal totalPaidAmount = paidPayments.stream()
			.filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(threshold))
			.map(Payment::getAmount)
			.filter(Objects::nonNull)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		// 4. 멤버십 조회 or 생성
		MemberShip memberShip = memberShipRepository.findByUser(user);
		if (memberShip == null) {
			memberShip = memberShipRepository.save(
				new MemberShip(user, Grade.NORMAL, null) // 만료기한 논의 필요
			);
		}

		// 6. 누적금액 기준으로 등급 재계산 (결제 완료/취소 시마다 계산을 다시하기 때문에 등급의 변화가 알아서 적용되는 개념)
		Grade newGrade = Grade.decideGrade(totalPaidAmount);
		memberShip.updateGrade(newGrade);
		memberShip.extendExpiresAt(now.plusDays(90));
	}
}
