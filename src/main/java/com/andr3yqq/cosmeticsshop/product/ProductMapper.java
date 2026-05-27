package com.andr3yqq.cosmeticsshop.product;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toProductDTO(Product product);

    Product toProduct(ProductDTO productDTO);

    List<ProductDTO> toProductDTOList(List<Product> products);

    List<Product> toProductList(List<ProductDTO> productDTOS);
}
