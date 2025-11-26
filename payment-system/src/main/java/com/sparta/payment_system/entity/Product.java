package com.sparta.payment_system.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long productId;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "stock", nullable = false)
	private Integer stock = 0;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	public Product(String name, BigDecimal price, Integer stock, String description) {
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.description = description;
	}

	// 재고 차감
	public void decreaseStock(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다.");
		}
		if (this.stock < quantity) {
			throw new IllegalStateException("상품 재고가 부족합니다. (현재 재고 = " + stock + ")");
		}
		this.stock -= quantity;
	}

	// 재고 원복
	public void rollbackStock(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("원복 수량은 1 이상이어야 합니다.");
		}
		this.stock += quantity;
	}

}
