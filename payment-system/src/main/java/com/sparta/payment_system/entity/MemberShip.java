package com.sparta.payment_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
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

    //expires+a_at 간에 회원이 결제한 총 금액
    @Column(name = "total_amount")
    BigDecimal totalAmount;

    @Column(name = "expires_at")
	private LocalDateTime expiresAt;

	public MemberShip(User user, Grade point, LocalDateTime expiresAt) {
		this.user = user;
		this.point = point;
		this.expiresAt = expiresAt;
	}
}
