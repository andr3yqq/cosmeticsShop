package com.andr3yqq.cosmeticsshop.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    @Email(message = "Email must be valid")
    private String email;
    @Min(value = 8, message = "Password must be at least 8 characters")
    private String password;
    private Long addressId;
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    private String firstName;
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    private String lastName;
    private String phoneNumber;
    private String role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
