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

			// 안전하게 저장 (dirty checking이 있더라도 명시적으로 save 해주면 디버깅이 쉬움)
			productRepository.save(product);

			System.out.println("[재고차감] productId=" + productId + " after=" + product.getStock());
		}
	}

	// 결제 취소(주문 취소) 후 재고 원복
	public void rollbackStockForOrder(Long orderId) {
		Order order = orderRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		List<OrderItem> items = orderItemRepository.findAllByOrder(order);

		for (OrderItem orderItem : items) {
			Product product = orderItem.getProduct();
			int quantity = orderItem.getQuantity();
			product.rollbackStock(quantity);
		}
	}
}
