package com.andr3yqq.cosmeticsshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private ProductCategory category;

    @NotNull(message = "Status cannot be null")
    private ProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product")
    private List<Image> images;

    private List<String> features;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
