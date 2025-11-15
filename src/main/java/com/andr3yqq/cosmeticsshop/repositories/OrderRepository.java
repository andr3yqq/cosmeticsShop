package com.andr3yqq.cosmeticsshop.repositories;

import com.andr3yqq.cosmeticsshop.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
