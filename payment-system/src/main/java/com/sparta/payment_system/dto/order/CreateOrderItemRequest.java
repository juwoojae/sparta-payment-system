package com.sparta.payment_system.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderItemRequest {

	@NotNull(message = "productId는 필수값입니다.")
	private Long productId;

	@Min(value = 1, message = "quantity는 최소 1개 이상이어야 합니다.")
	@Max(value = 1000, message = "quantity는 최대 1000개까지 가능합니다.")
	private int quantity;

}
