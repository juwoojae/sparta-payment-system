package com.sparta.payment_system.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelPaymentResponse {
	private String impUid;
	private String reason;
}
