package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.brand.BrandDTO;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDTO productDTO);
    Product updateProduct(ProductDTO productDTO);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product getProductBySku(String sku);
    List<Product> getProductsByBrand(BrandDTO brandDTO);
}
