package com.andr3yqq.cosmeticsshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Discount name cannot be blank")
    private String name;
    private String description;
    @Positive(message = "Discount percentage must be greater than zero")
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Product> appliedToProducts;
    private List<ProductCategory> appliedToCategories;
    private List<User> appliedToUsers;
    private List<Brand> appliedToBrands;
}
