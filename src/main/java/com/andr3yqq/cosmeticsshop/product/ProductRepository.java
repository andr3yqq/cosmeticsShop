package com.andr3yqq.cosmeticsshop.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findBySku(String sku);

    List<Product> findAllByBrand(String brand);

    List<Product> findAllByCategory(ProductCategory category);

    List<Product> findAllByName(String name);
}
