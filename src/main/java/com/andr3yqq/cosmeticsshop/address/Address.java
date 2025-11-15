package com.andr3yqq.cosmeticsshop.address;

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
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "streetLine1", nullable = false)
    private String streetLine1;
    private String streetLine2;
    @Column(name = "city", nullable = false)
    private String city;
    @Column(name = "state", nullable = false)
    private String state;
    @Column(name = "zipcode", nullable = false)
    private String zipcode;
    @Column(name = "country", nullable = false)
    private String country;
}