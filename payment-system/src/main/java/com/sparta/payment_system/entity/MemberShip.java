package com.sparta.payment_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "memberships")
@NoArgsConstructor
public class MemberShip extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "membership_id")
	private Long MemberShipId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	//회원
	@Enumerated(EnumType.STRING)
	Grade grade;

	//expires+a_at 간에 회원이 결제한 총 금액
	@Column(name = "total_amount")
	BigDecimal totalAmount;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	public MemberShip(User user, Grade grade, LocalDateTime expiresAt) {
		this.user = user;
		this.grade = grade;
		this.expiresAt = expiresAt;
	}

	public void updateGrade(Grade newGrade) {
		this.grade = newGrade;
	}

	public void updateTotalAmount(BigDecimal amount) {
		this.totalAmount = amount;
	}

	public void extendExpiresAt(LocalDateTime newExpiresAt) {
		this.expiresAt = newExpiresAt;
	}
}
