package com.andr3yqq.cosmeticsshop.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    @Email(message = "Email must be valid")
    private String email;
    private Long addressId;
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    private String firstName;
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    private String lastName;
    private String phoneNumber;
    private String role;
    private boolean isActive;
}
