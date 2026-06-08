package com.andr3yqq.cosmeticsshop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findBySku(String sku);

    Page<Product> findAllByBrand(String brand, Pageable pageable);

    Page<Product> findAllByCategory(ProductCategory category, Pageable pageable);

    Page<Product> findAllByName(String name, Pageable pageable);
}
