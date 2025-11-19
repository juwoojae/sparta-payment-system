// package com.sparta.payment_system.controller;
//
// import java.util.List;
// import java.util.Map;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import com.sparta.payment_system.entity.Order;
// import com.sparta.payment_system.entity.Payment;
// import com.sparta.payment_system.entity.PaymentStatus;
// import com.sparta.payment_system.repository.OrderItemRepository;
// import com.sparta.payment_system.repository.OrderRepository;
// import com.sparta.payment_system.repository.PaymentRepository;
// import com.sparta.payment_system.repository.ProductRepository;
// import com.sparta.payment_system.service.PaymentService;
//
// import reactor.core.publisher.Mono;
//
// @RestController
// @RequestMapping("/api/payments")
// @CrossOrigin(origins = "*")
// public class PaymentController {
//
// 	private final PaymentService paymentService;
// 	private final OrderRepository orderRepository;
// 	private final PaymentRepository paymentRepository;
// 	private final ProductRepository productRepository;
// 	private final OrderItemRepository orderItemRepository;
//
// 	@Autowired
// 	public PaymentController(PaymentService paymentService, OrderRepository orderRepository,
// 		OrderItemRepository orderItemRepository, PaymentRepository paymentRepository,
// 		ProductRepository productRepository) {
// 		this.paymentService = paymentService;
// 		this.orderRepository = orderRepository;
// 		this.orderItemRepository = orderItemRepository;
// 		this.paymentRepository = paymentRepository;
// 		this.productRepository = productRepository;
// 	}
// 	/* 결제 요청은 프론트엔드에서 진행 (KG 이니시스 - 포트원 결제)
// 	 * 결제 완료 후의 결과값을 받아와 검증 및 상태값 변경 상태로 진행할 로직으로 설정 필요 */
//
// 	// 결제 완료 검증 API
// 	@PostMapping("/complete")
// 	public Mono<ResponseEntity<String>> completePayment(@RequestBody Map<String, String> request) {
// 		String paymentId = request.get("paymentId");
// 		System.out.println("결제 완료 검증 요청 받음 - Payment ID: " + paymentId);
//
// 		return paymentService.verifyPayment(paymentId)
// 			.map(isSuccess -> {
// 				if (isSuccess) {
// 					return ResponseEntity.ok("Payment verification successful.");
// 				} else {
// 					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed.");
// 				}
// 			});
// 	}
//
// 	// 결제 취소 API
// 	@PostMapping("/cancel")
// 	public Mono<ResponseEntity<String>> cancelPaymentByPaymentId(@RequestBody Map<String, String> request) {
// 		Long paymentId = request.get("paymentId");
// 		String reason = request.getOrDefault("reason", "사용자 요청에 의한 취소");
//
// 		return paymentService.cancelPayment(paymentId, reason)
// 			.map(isSuccess -> {
// 				if (isSuccess) {
// 					return ResponseEntity.ok("Payment cancellation successful.");
// 				} else {
// 					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment cancellation failed.");
// 				}
// 			});
// 	}
//
// 	// PAID 상태의 결제 목록 조회 (환불 가능한 결제들)
// 	@GetMapping("/paid")
// 	public ResponseEntity<List<Payment>> getPaidPayments() {
// 		try {
// 			List<Payment> paidPayments = paymentRepository.findByStatus(PaymentStatus.PAID);
// 			return ResponseEntity.ok(paidPayments);
// 		} catch (Exception e) {
// 			System.err.println("PAID 결제 목록 조회 오류: " + e.getMessage());
// 			e.printStackTrace();
// 			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
// 		}
// 	}
//
// 	// 특정 사용자의 PAID 결제 목록 조회
// 	@GetMapping("/paid/user/{userId}")
// 	public ResponseEntity<List<Payment>> getPaidPaymentsByUser(@PathVariable Long userId) {
// 		try {
// 			// 주문을 통해 사용자별 결제 조회
// 			List<Order> userOrders = orderRepository.findByUserId(userId);
// 			List<Long> orderIds = userOrders.stream()
// 				.map(Order::getOrderId)
// 				.toList();
//
// 			List<Payment> paidPayments = paymentRepository.findByOrderIdInAndStatus(orderIds,
// 				PaymentStatus.PAID);
// 			return ResponseEntity.ok(paidPayments);
// 		} catch (Exception e) {
// 			System.err.println("사용자별 PAID 결제 목록 조회 오류: " + e.getMessage());
// 			e.printStackTrace();
// 			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
// 		}
// 	}
// }
