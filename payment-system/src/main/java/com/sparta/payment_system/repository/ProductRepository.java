package com.sparta.payment_system.repository;

import com.sparta.payment_system.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findById(Long userId);

    //List<Product> findByNameContainingIgnoreCase(String name);

    //List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    //List<Product> findByStockGreaterThan(Integer stock);

    // 새로 추가된 메서드들
    //List<Product> findByStatus(ProductStatus status);

    //List<Product> findByStockLessThanEqual(Integer stock);
}
