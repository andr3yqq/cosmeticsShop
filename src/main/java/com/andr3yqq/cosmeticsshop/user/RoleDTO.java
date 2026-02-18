package com.andr3yqq.cosmeticsshop.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {
    private Long id;
    @NotBlank(message = "Name cannot be blank")
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
