package com.sparta.payment_system.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sparta.payment_system.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPaidPaymentListResponse {
	private Long paymentId;
	private Long orderId;
	private String impUid;
	private BigDecimal paymentAmount;
	private String paymentMethod;

	private PaymentStatus paymentStatus;

	private boolean canRefund;
}
