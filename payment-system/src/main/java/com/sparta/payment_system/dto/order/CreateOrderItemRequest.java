package com.sparta.payment_system.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderItemRequest {

	private Long productId;
	private int quantity;

}
