package com.sparta.payment_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.payment_system.dto.point.GetPointsRequest;
import com.sparta.payment_system.dto.point.GetPointsResponse;
import com.sparta.payment_system.entity.Grade;
import com.sparta.payment_system.entity.MemberShip;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.PointTransaction;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.repository.MemberShipRepository;
import com.sparta.payment_system.repository.PointTransactionRepository;

@Service
public class PointService {
	private final PointTransactionRepository pointTransactionRepository;
	private final MemberShipRepository memberShipRepository;

	public PointService(PointTransactionRepository pointTransactionRepository,
		MemberShipRepository memberShipRepository) {
		this.pointTransactionRepository = pointTransactionRepository;
		this.memberShipRepository = memberShipRepository;
	}

	/* 결제 완료 후 포인트 적립
	 * - 현재 정책 : 등급별 결제 금액의 일정 비율을 포인트로 적립 (소수점 이하 제거)
	 * - 추후 정책 변경 시 이 메서드 수정 필요
	 */
	public void earnPointsAfterPayment(Order order, BigDecimal paidAmount) {
		if (order == null || paidAmount == null) {
			return;
		}

		if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}

		User user = order.getUser();
		if (user == null) {
			return;
		}

		// 등급별 결제 금액의 일정 비율을 적립
		MemberShip memberShip = memberShipRepository.findByUser(user);
		if (memberShip == null) {
			return;
		}

		Grade grade = memberShip.getGrade();
		// 1, 5, 10 비율을 소수로 바꾸는 과정
		BigDecimal rate = BigDecimal.valueOf(grade.getRate())
			.divide(BigDecimal.valueOf(100));

		// 결제 금액 * 등급별 적립률
		int points = paidAmount
			.multiply(rate)
			.setScale(0, RoundingMode.DOWN)
			.intValue();

		if (points <= 0) {
			return;
		}

		LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

		PointTransaction pointTransaction = new PointTransaction(
			user,
			order,
			points,
			expiresAt
		);

		pointTransactionRepository.save(pointTransaction);
	}

	/* 결제 전액 취소 시 포인트 회수
	 * - 해당 주문과 관련된 포인트 지급,회수에 대한 과정을 netPoint로 계산한 뒤
	 * netPoints가 양수인 경우 그만큼 음수 포인트를 추가로 기록하여 회수처리를 진행함 (향후 부분환불 로직 고려한 개념)
	 * - 이미 한 번 이상 회수된 주문에 대해서는 중복 회수되지 않도록 기준 설정
	 */
	public void rollbackPointsAfterCancel(Order order) {
		if (order == null) {
			return;
		}

		User user = order.getUser();
		if (user == null) {
			return;
		}

		// 취소 요청 주문과 관련된 모든 포인트 트랜잭션 조회
		List<PointTransaction> transactions =
			pointTransactionRepository.findByUserAndOrder(user, order);
		if (transactions == null || transactions.isEmpty()) {
			return; // 이력 없으면 회수할 데이터 없음
		}

		int netPoints = transactions.stream()
			.mapToInt(PointTransaction::getPoints)
			.sum();

		if (netPoints > 0) {
			PointTransaction rollbackPointTransaction = new PointTransaction(
				user,
				order,
				-netPoints,
				null
			);
			pointTransactionRepository.save(rollbackPointTransaction);
		}
	}

	//모든 pointTransaction 에서 userId 인 것만 찾은후, totalPoints 계산하기
	public GetPointsResponse getUserTotalPoints(GetPointsRequest getPointsRequest) {

		int points = pointTransactionRepository.getTotalPointsByUserId(getPointsRequest.getUserId());
		return new GetPointsResponse(points);
	}
}
