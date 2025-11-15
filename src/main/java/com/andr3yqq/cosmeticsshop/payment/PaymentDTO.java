package com.andr3yqq.cosmeticsshop.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long orderId;
    @NotNull(message = "Payment status cannot be null")
    private String status;
    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod;
    @NotNull(message = "Transaction ID cannot be null")
    private String transactionId;
    @PositiveOrZero(message = "Amount must be greater than or equal to zero")
    private Double amount;
    @NotNull(message = "Currency cannot be null")
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
