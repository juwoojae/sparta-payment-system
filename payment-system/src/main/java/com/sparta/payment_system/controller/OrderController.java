package com.sparta.payment_system.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.ProductRepository;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;

	@Autowired
	public OrderController(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
		ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
	}

	@PostMapping
	public ResponseEntity<Order> createOrder(@RequestBody Order order) {
		try {
			// 주문 저장
			Order savedOrder = orderRepository.save(order);

			// 주문 아이템들 저장
			if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
				for (OrderItem orderItem : order.getOrderItems()) {
					// 상품 존재 여부 확인
					Optional<Product> productOptional = productRepository.findById(orderItem.getProductId());
					if (productOptional.isEmpty()) {
						System.err.println("상품을 찾을 수 없습니다. Product ID: " + orderItem.getProductId());
						return ResponseEntity.badRequest().build();
					}

					orderItem.setOrderId(savedOrder.getOrderId());
					orderItemRepository.save(orderItem);
				}
				System.out.println("주문 아이템 " + order.getOrderItems().size() + "개가 저장되었습니다.");
			}

			return ResponseEntity.ok(savedOrder);
		} catch (Exception e) {
			System.err.println("주문 생성 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		try {
			List<Order> orders = orderRepository.findAll();
			return ResponseEntity.ok(orders);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

}
