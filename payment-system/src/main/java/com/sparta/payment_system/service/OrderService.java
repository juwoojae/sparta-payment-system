package com.sparta.payment_system.service;

import static com.sparta.payment_system.entity.OrderStatus.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.payment_system.dto.order.CreateOrderItemRequest;
import com.sparta.payment_system.dto.order.CreateOrderItemResponse;
import com.sparta.payment_system.dto.order.CreateOrderRequest;
import com.sparta.payment_system.dto.order.CreateOrderResponse;
import com.sparta.payment_system.dto.order.GetOrderItemResponse;
import com.sparta.payment_system.dto.order.GetOrderResponse;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.ProductRepository;
import com.sparta.payment_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {

		Long userId = createOrderRequest.getUserId();
		User user = userRepository.findById(userId).orElseThrow(
			() -> new IllegalArgumentException("존재하지 않는 user")
		);

		BigDecimal totalAmount = BigDecimal.ZERO;

		Order order = new Order(user, totalAmount, PENDING_PAYMENT);
		orderRepository.save(order);

		List<CreateOrderItemResponse> orderItems = new ArrayList<>();

		for (CreateOrderItemRequest orderItem : createOrderRequest.getOrderItems()) {
			Product product = productRepository.findById(orderItem.getProductId()).orElseThrow(
				() -> new IllegalStateException("존재하지 않는 상품")
			);

			BigDecimal price = product.getPrice();
			BigDecimal totalPrice =price.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
			totalAmount = totalAmount.add(totalPrice);

			OrderItem saved =OrderItem.builder()
				.order(order)
				.product(product)
				.quantity(orderItem.getQuantity())
				.price(price)
				.totalPrice(totalPrice)
				.build();
			orderItemRepository.save(saved);

			orderItems.add(CreateOrderItemResponse.builder()
				.productId(product.getProductId())
				.quantity(orderItem.getQuantity())
				.price(price)
				.totalPrice(totalPrice)
				.build());
		}

		order.updateTotalAmount(totalAmount);

		return CreateOrderResponse.builder()
				.userId(userId)
				.orderId(order.getOrderId())
				.totalAmount(totalAmount)
				.orderStatus(PENDING_PAYMENT)
				.orderItems(orderItems)
				.createdAt(order.getCreatedAt())
				.build();

	}

	public List<GetOrderResponse> getOrders() {

		List<GetOrderResponse> orderResponses = new ArrayList<>();

		//OrderStatus 가 PENDING_PAYMENT 인 조건만 전체 조회
		for (Order order : orderRepository.findAllByStatus(PENDING_PAYMENT)) {

			List<GetOrderItemResponse> orderItemResponses = new ArrayList<>();

			for (OrderItem orderItem : orderItemRepository.findAllByOrder(order)) {
				orderItemResponses.add(GetOrderItemResponse.builder()
					.productId(orderItem.getProduct().getProductId())
					.quantity(orderItem.getQuantity())
					.price(orderItem.getPrice())
					.totalPrice(orderItem.getTotalPrice())
					.build());
			}

			orderResponses.add(GetOrderResponse.builder()
					.userId(order.getUser().getUserId())
					.orderId(order.getOrderId())
					.totalAmount(order.getTotalAmount())
					.orderStatus(PENDING_PAYMENT)
					.orderItems(orderItemResponses)
					.createdAt(order.getCreatedAt())
					.build());
		}
		return orderResponses;
	}
}
