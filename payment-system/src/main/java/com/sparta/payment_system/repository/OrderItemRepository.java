package com.sparta.payment_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findAllByOrder(Order order);

	List<OrderItem> findByOrder_OrderId(Long orderId);
}
