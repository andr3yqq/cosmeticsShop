package com.andr3yqq.cosmeticsshop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Product createProduct(ProductDTO productDTO);
    Product updateProduct(ProductDTO productDTO);
    Page<Product> getAllProducts(Pageable pageable);
    Product getProductById(Long id);
    Product getProductBySku(String sku);
    Page<Product> getProductsByBrand(String brand, Pageable pageable);
    Page<Product> getProductsByCategory(String category, Pageable pageable);
    Page<Product> getProductsByName(String name, Pageable pageable);
}
