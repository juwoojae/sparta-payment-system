package com.sparta.payment_system.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.ProductRepository;

@Service
public class ProductService {
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;

	public ProductService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
		ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
	}

	// 결제 성공 후 재고 차감
	@Transactional
	public void decreaseStockForOrder(Long orderId) {
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		List<OrderItem> items = orderItemRepository.findAllByOrder(order);

		for (OrderItem orderItem : items) {
			Long productId = orderItem.getProduct().getProductId();

			Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

			int quantity = orderItem.getQuantity();

			System.out.println(
				"[재고차감] productId=" + productId + " before=" + product.getStock() + " minus=" + quantity);

			product.decreaseStock(quantity);

			productRepository.save(product);

			System.out.println("[재고차감] productId=" + productId + " after=" + product.getStock());
		}
	}

	// 결제 취소(주문 취소) 후 재고 원복
	@Transactional
	public void rollbackStockForOrder(Long orderId) {

		// 1) 주문 다시 조회
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		// 2) 주문에 속한 모든 OrderItem 조회
		List<OrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);

		// 3) 각 OrderItem 기준으로 Product 찾아서 재고 원복
		for (OrderItem orderItem : items) {
			Long productId = orderItem.getProduct().getProductId();

			Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + productId));

			int quantity = orderItem.getQuantity();

			System.out.println(
				"[재고원복] productId=" + productId + " before=" + product.getStock() + " plus=" + quantity);

			product.rollbackStock(quantity);

			productRepository.save(product);

			System.out.println("[재고원복] productId=" + productId + " after=" + product.getStock());
		}
	}
}
