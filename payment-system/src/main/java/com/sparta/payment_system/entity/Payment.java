package com.sparta.payment_system.entity;

import java.math.BigDecimal;

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
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id", unique = true)
	private Long paymentId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	/* 환불 시 사용되는 포트원 시스템에서 발급하는 결제 트랜잭션 고유 ID
	 * imp_123456789012와 같은 형식으로 String으로 설정 필요 */
	@Column(name = "imp_uid", unique = true, length = 255)
	private String impUid;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private PaymentStatus status;

	@Column(name = "payment_method", length = 100)
	private String paymentMethod;

	public Payment(Order order, BigDecimal amount, String impUid, PaymentStatus status, String paymentMethod) {
		this.order = order;
		this.amount = amount;
		this.impUid = impUid;
		this.status = status;
		this.paymentMethod = paymentMethod;
	}

	public void completePayment(BigDecimal amount, String method) {
		this.amount = amount;
		this.status = PaymentStatus.PAID;
		this.paymentMethod = method;
	}

	public void updatePaymentStatus(PaymentStatus status) {
		this.status = status;
	}

}
