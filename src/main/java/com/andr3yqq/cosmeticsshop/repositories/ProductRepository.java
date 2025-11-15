package com.andr3yqq.cosmeticsshop.repositories;

import com.andr3yqq.cosmeticsshop.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
