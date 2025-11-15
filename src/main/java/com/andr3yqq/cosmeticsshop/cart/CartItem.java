package com.andr3yqq.cosmeticsshop.cart;

import com.andr3yqq.cosmeticsshop.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "moment_price", nullable = false)
    private Double momentPrice;
}
