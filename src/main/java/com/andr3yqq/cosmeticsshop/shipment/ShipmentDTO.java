package com.andr3yqq.cosmeticsshop.shipment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentDTO {
    private Long id;
    private Long orderId;
    private String address;
    private String trackingNumber;
    @NotNull(message = "Shipping provider cannot be null")
    private String shippingProvider;
    @NotNull(message = "Status cannot be null")
    private String status;
    private Double shippingCost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
