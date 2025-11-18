package com.sparta.payment_system.entity;

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
	Grade point;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	public MemberShip(User user, Grade point, LocalDateTime expiresAt) {
		this.user = user;
		this.point = point;
		this.expiresAt = expiresAt;
	}
}
