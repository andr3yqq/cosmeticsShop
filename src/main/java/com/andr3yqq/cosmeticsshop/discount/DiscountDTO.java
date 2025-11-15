package com.andr3yqq.cosmeticsshop.discount;

import com.andr3yqq.cosmeticsshop.brand.BrandDTO;
import com.andr3yqq.cosmeticsshop.product.ProductDTO;
import com.andr3yqq.cosmeticsshop.user.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDTO {
    private Long id;
    @NotBlank(message = "Discount name cannot be blank")
    private String name;
    private String description;
    @Positive(message = "Discount percentage must be greater than zero")
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ProductDTO> appliedToProducts;
    private List<String> appliedToCategories;
    private List<UserDTO> appliedToUsers;
    private List<BrandDTO> appliedToBrands;
}
