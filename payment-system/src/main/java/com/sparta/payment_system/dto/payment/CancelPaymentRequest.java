package com.sparta.payment_system.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CancelPaymentRequest {

	@NotNull(message = "impUid 필수값입니다.")
	private String impUid;
	private String reason;
}
