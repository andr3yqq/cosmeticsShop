package com.andr3yqq.cosmeticsshop.review;

import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rating", nullable = false)
    private Integer rating;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private String comment;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
