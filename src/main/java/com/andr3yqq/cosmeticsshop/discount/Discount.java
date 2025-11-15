package com.andr3yqq.cosmeticsshop.discount;

import com.andr3yqq.cosmeticsshop.brand.Brand;
import com.andr3yqq.cosmeticsshop.product.Product;
import com.andr3yqq.cosmeticsshop.product.ProductCategory;
import com.andr3yqq.cosmeticsshop.user.User;
import jakarta.persistence.*;
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
    @Column(name = "name", nullable = false)
    private String name;
    private String description;
    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Product> appliedToProducts;
    @Enumerated(EnumType.STRING)
    private List<ProductCategory> appliedToCategories;
    private List<User> appliedToUsers;
    private List<Brand> appliedToBrands;
}
