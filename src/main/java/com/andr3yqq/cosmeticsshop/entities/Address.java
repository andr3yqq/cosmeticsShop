package com.andr3yqq.cosmeticsshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Street line 1 cannot be blank")
    private String streetLine1;
    private String streetLine2;
    @NotBlank(message = "City cannot be blank")
    private String city;
    @NotBlank(message = "State cannot be blank")
    private String state;
    @NotBlank(message = "Zipcode cannot be blank")
    private String zipcode;
    @NotBlank(message = "Country cannot be blank")
    private String country;
}