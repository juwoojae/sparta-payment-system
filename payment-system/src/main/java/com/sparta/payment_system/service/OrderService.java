package com.sparta.payment_system.service;

import static com.sparta.payment_system.entity.OrderStatus.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sparta.payment_system.dto.order.*;
import com.sparta.payment_system.exception.InvalidProductQuantityException;
import com.sparta.payment_system.exception.NotFoundException;
import com.sparta.payment_system.exception.UnauthorizedActionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.repository.OrderItemRepository;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.ProductRepository;
import com.sparta.payment_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "OrderService")
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {

        Long userId = createOrderRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("존재하지 않는 user")
        );

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = new Order(user, totalAmount, PENDING_PAYMENT);
        orderRepository.save(order);

        List<CreateOrderItemResponse> orderItems = new ArrayList<>();

        for (CreateOrderItemRequest orderItem : createOrderRequest.getOrderItems()) {
            Product product = productRepository.findById(orderItem.getProductId()).orElseThrow(
                    () -> new NotFoundException("존재하지 않는 상품")
            );

            //도메인 예외 - 재고보다 주문이 많이 들어온 경우
            if (product.getStock() - orderItem.getQuantity() <= 0) {
                throw new InvalidProductQuantityException("재고가 부족합니다");
            }

            BigDecimal price = product.getPrice();
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            totalAmount = totalAmount.add(totalPrice);

            OrderItem saved = OrderItem.builder()
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

    /**
     * 단건 조회 (결제 폼)
     * 자신이 생성한 주문에 대해서만 조회할수있고,
     * 자신의 주문이 아닌경우에는 조회를 허용하지 않는다
     */
    public GetOrderResponse getOrder(Long userId, Long orderId) {

        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new NotFoundException("존재하지 않는 orderId")
        );

        if (!order.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedActionException("해당 orderId 를 불러올 권한이 없습니다");
        }
        //만약 조회 되었을때 PENDING_PAYMENT 가 아니라면 이미 결제된 주문이거나, 취소된 주문이므로 예외를 던진다.
        if (!order.getStatus().equals(PENDING_PAYMENT)) {
            throw new NotFoundException("존재하지 않는 orderId");
        }

        //권한 검증 - 해당 order 가 인가된 user 가 생성한 order 가 맞는지


        List<GetOrderItemResponse> orderItemResponses = new ArrayList<>();

        for (OrderItem orderItem : orderItemRepository.findAllByOrder(order)) {
            orderItemResponses.add(GetOrderItemResponse.builder()
                    .productId(orderItem.getProduct().getProductId())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice())
                    .totalPrice(orderItem.getTotalPrice())
                    .build());
        }
        return GetOrderResponse.builder()
                .userId(order.getUser().getUserId())
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(PENDING_PAYMENT)
                .orderItems(orderItemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
