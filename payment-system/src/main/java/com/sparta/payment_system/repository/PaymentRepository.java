package com.sparta.payment_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.Payment;
import com.sparta.payment_system.entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	//Optional<Payment> findByOrderId(Long orderId);

	//Optional<Payment> findByImpUid(String impUid);

	//List<Payment> findByStatus(PaymentStatus status);

	//List<Payment> findByMethodId(Long methodId);

	//  신규 추가 부분
	//List<Payment> findByOrderIdInAndStatus(List<Long> orderIds, PaymentStatus status);

	Long order(Order order);
}
