package com.sparta.payment_system;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sparta.payment_system.entity.PointTransaction;
import com.sparta.payment_system.entity.Product;
import com.sparta.payment_system.entity.User;
import com.sparta.payment_system.repository.OrderRepository;
import com.sparta.payment_system.repository.PointTransactionRepository;
import com.sparta.payment_system.repository.ProductRepository;
import com.sparta.payment_system.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TestDataInit {

	private final ProductRepository productRepository;
	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
	private final PointTransactionRepository pointTransactionRepository;

	@PostConstruct
	public void init(){
		productRepository.save(new Product("아메리카노",new BigDecimal(1500),100,"와이리 밍밍하노"));
		productRepository.save(new Product("아이스티",new BigDecimal(2000),100,"커피를 못먹는"));
		productRepository.save(new Product("아샷추",new BigDecimal(1800),50,"아메리카노에 샷 추가"));

		User test = new User("test@naver.com",passwordEncoder.encode("123456"), "주우재");
		userRepository.save(test);
	}
}
