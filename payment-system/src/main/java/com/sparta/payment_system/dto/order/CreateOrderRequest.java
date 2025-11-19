package com.sparta.payment_system.dto.order;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

	private Long userId;
	List<CreateOrderItemRequest> orderItems;

}
