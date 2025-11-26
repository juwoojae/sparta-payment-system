package com.sparta.payment_system.dto.order;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetOrderItemResponse {

	private Long productId;
	private int quantity;
	private BigDecimal price;
	private BigDecimal totalPrice;
}
