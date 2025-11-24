package com.sparta.payment_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.PointTransaction;
import com.sparta.payment_system.entity.User;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

	List<PointTransaction> findByUserAndOrder(User user, Order order);

	@Query("SELECT COALESCE(SUM(p.points), 0) FROM PointTransaction p WHERE p.user.userId = :userId")
	int getTotalPointsByUserId(Long userId);
}
