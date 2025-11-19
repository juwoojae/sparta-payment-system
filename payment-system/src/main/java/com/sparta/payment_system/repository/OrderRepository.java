package com.sparta.payment_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

	//Optional<Order> findByOrderId(String orderId);

	//List<Order> findByUserId(Long userId);

	List<Order> findAllByStatus(OrderStatus orderStatus);

	//List<Order> findByStatus(OrderStatus status);

	//List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

	//Optional<Order> findByOrderId(Long orderId);
}
