package com.andr3yqq.cosmeticsshop.repositories;

import com.andr3yqq.cosmeticsshop.entities.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
