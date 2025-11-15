package com.andr3yqq.cosmeticsshop.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {
    private Long id;

    @NotBlank(message = "Brand name cannot be blank")
    private String name;

    private String description;

    private Long imageId;
}
