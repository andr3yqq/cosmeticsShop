package com.andr3yqq.cosmeticsshop.repositories;

import com.andr3yqq.cosmeticsshop.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
