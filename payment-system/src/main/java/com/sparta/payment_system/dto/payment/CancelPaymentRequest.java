package com.sparta.payment_system.dto.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelPaymentRequest {
	private String impUid;
	private String reason;
}
