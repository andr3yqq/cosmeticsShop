package com.andr3yqq.cosmeticsshop.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {
    private Long id;
    @NotBlank(message = "Name cannot be blank")
    private String name;
}
