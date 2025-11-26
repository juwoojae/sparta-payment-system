package com.sparta.payment_system.dto.order;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

	@NotNull(message = "user_id는 필수값입니다.")
	private Long userId;
	List<CreateOrderItemRequest> orderItems;

}
