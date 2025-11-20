package com.sparta.payment_system.dto.payment;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class VerifyPaymentRequest {
	private final Long orderId;
	private final String impUid;
	private final BigDecimal amount;

	public VerifyPaymentRequest(Long orderId, String impUid, BigDecimal amount) {
		this.orderId = orderId;
		this.impUid = impUid;
		this.amount = amount;
	}
}
