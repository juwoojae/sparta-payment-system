package com.sparta.payment_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sparta.payment_system.entity.Order;
import com.sparta.payment_system.entity.Payment;
import com.sparta.payment_system.entity.PaymentStatus;
import com.sparta.payment_system.entity.User;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrder(Order order);

	Optional<Payment> findByImpUid(String impUid);

	List<Payment> findAllByOrder_UserAndStatus(User user, PaymentStatus paymentStatus);

}
