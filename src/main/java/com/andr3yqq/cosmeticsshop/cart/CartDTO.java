package com.andr3yqq.cosmeticsshop.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private Long id;
    private Long userId;
    private LocalDateTime updatedAt;
    private List<CartItemDTO> cartItems;
}
