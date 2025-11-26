package com.sparta.payment_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

	List<Order> findAllByStatus(OrderStatus orderStatus);

    Optional<Order> findByOrderId(Long orderId);
}
