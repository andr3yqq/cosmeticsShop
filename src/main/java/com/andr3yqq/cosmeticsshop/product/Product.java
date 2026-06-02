package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.image.Image;
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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(name="brand", nullable = false)
    private String brand;

    @Column(name="name", nullable = false)
    private String name;

    private String description;

    @Column(name="price", nullable = false)
    private Double price;

    @Column(name="last_price", nullable = false)
    private Double lastPrice;

    @Column(name="available_stock", nullable = false)
    private Long availableStock;

    @Column(name="category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @ElementCollection
    private List<String> features;

    public Product(String sku, String brand, String name, String description, Double price, Long availableStock, ProductStatus status, ProductCategory category) {
        this.sku = sku;
        this.brand = brand;
        this.name = name;
        this.description = description;
        this.price = price;
        this.lastPrice = price;
        this.availableStock = availableStock;
        this.status = status;
        this.category = category;
    }

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
