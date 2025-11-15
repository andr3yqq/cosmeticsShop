package com.andr3yqq.cosmeticsshop.cart;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private Long cartId;
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
    @PositiveOrZero(message = "Price must be greater than or equal to zero")
    private Double momentPrice;
}
