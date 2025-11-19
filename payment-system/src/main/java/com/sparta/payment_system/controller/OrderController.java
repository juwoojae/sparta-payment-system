package com.sparta.payment_system.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.dto.order.CreateOrderRequest;
import com.sparta.payment_system.dto.order.CreateOrderResponse;
import com.sparta.payment_system.dto.order.GetOrderResponse;
import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.ProductRepository;
import com.sparta.payment_system.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {

		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(createOrderRequest));
	}

	@GetMapping
	public ResponseEntity<List<GetOrderResponse>> getAllOrders() {

		return ResponseEntity.status(HttpStatus.OK).body(orderService.getOrders());
	}

}
