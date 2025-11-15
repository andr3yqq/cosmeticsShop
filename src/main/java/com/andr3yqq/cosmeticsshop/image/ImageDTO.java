package com.andr3yqq.cosmeticsshop.image;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDTO {
    private Long id;
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;
    private String name;
    @NotBlank(message = "Type cannot be blank")
    private String type;
}
