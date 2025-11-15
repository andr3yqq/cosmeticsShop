package com.andr3yqq.cosmeticsshop.repositories;

import com.andr3yqq.cosmeticsshop.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
