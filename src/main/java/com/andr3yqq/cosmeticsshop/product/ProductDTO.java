package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.image.ImageDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "SKU cannot be blank")
    private String sku;

    @NotBlank(message = "Brand cannot be blank")
    private String brand;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String description;

    @PositiveOrZero(message = "Price must be greater than or equal to zero")
    private Double price;

    @PositiveOrZero(message = "Last price must be greater than or equal to zero")
    private Double lastPrice;

    @PositiveOrZero(message = "Stock must be greater than or equal to zero")
    private Long availableStock;

    @NotNull(message = "Category cannot be null")
    private String category;

    @NotNull(message = "Status cannot be null")
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ImageDTO> images;

    private List<String> features;
}
