package com.andr3yqq.cosmeticsshop.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    @NotNull(message = "Order number cannot be null")
    private String orderNumber;
    @NotNull(message = "Status cannot be null")
    private String status;
    @PositiveOrZero(message = "Total price must be greater than or equal to zero")
    private Double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
