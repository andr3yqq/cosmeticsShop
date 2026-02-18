package com.andr3yqq.cosmeticsshop.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    @Email(message = "Email must be valid")
    private String email;
    @Min(value = 8, message = "Password must be at least 8 characters")
    private String password;
}
