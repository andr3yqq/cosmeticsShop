package com.andr3yqq.cosmeticsshop.payment;

import com.andr3yqq.cosmeticsshop.order.Order;
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
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
    @Column(name = "amount", nullable = false)
    private Double amount;
    @Column(name = "currency", nullable = false)
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
