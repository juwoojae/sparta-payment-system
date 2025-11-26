package com.sparta.payment_system.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sparta.payment_system.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrderResponse {

	private Long userId;
	private Long orderId;
	private BigDecimal totalAmount;
	private OrderStatus orderStatus;
	private LocalDateTime createdAt;
	private List<CreateOrderItemResponse> orderItems;


}
