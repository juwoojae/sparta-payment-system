package com.sparta.payment_system.controller;

import java.util.List;
import java.util.Optional;

import ch.qos.logback.core.model.Model;
import com.sparta.payment_system.global.jwt.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sparta.payment_system.dto.order.CreateOrderRequest;
import com.sparta.payment_system.dto.order.CreateOrderResponse;
import com.sparta.payment_system.dto.order.GetOrderResponse;
import com.sparta.payment_system.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest) {

        return ResponseEntity.status(CREATED).body(orderService.createOrder(createOrderRequest));
    }

    @GetMapping
    public ResponseEntity<List<GetOrderResponse>> getAllOrder() {

        return ResponseEntity.status(OK).body(orderService.getOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(@PathVariable("orderId") Long orderId, @AuthenticationPrincipal UserInfo principal) {
        System.out.println("OrderController.getOrder 호출");
        Long userId = principal.getUserId();
        return ResponseEntity.status(OK).body(orderService.getOrder(userId, orderId));
    }
}
