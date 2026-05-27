package com.andr3yqq.cosmeticsshop.image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.andr3yqq.cosmeticsshop.product.Product;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    private String name;
    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Image(Long id, String imageUrl, String name, String type) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.type = type;
    }
}
