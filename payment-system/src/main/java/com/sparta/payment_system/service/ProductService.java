package com.sparta.payment_system.service;

import org.springframework.stereotype.Service;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;

@Service
public class ProductService {
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	public ProductService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
	}

	// 결제 성공 후 재고 차감
	public void decreaseStockForOrder(Long orderId) {
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		for (OrderItem orderItem : orderItemRepository.findAllByOrder(order)) {
			orderItem.decreaseProductStock();
		}
	}

	// 결제 취소(주문 취소) 후 재고 원복
	public void rollbackStockForOrder(Long orderId) {
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		for (OrderItem orderItem : orderItemRepository.findAllByOrder(order)) {
			orderItem.rollbackProductStock();
		}
	}
}
