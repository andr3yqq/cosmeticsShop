package com.andr3yqq.cosmeticsshop.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productMapper.toProductDTO(productService.createProduct(productDTO));
        if (createdProduct == null)
            return ResponseEntity.badRequest().body(productDTO);
        return ResponseEntity.ok().body(createdProduct);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productMapper.toProductDTO(productService.updateProduct(productDTO));
        if (updatedProduct == null)
            return ResponseEntity.badRequest().body(productDTO);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> allProducts(Pageable pageable) {
        Page<ProductDTO> products = productService.getAllProducts(pageable)
                .map(productMapper::toProductDTO);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<ProductDTO>> allProductsByBrand(@PathVariable String brand, Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByBrand(brand, pageable)
                .map(productMapper::toProductDTO);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDTO>> allProductsByCategory(@PathVariable String category, Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable)
                .map(productMapper::toProductDTO);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Page<ProductDTO>> allProductsByName(@PathVariable String name, Pageable pageable) {
        Page<ProductDTO> products = productService.getProductsByName(name, pageable)
                .map(productMapper::toProductDTO);
        return ResponseEntity.ok().body(products);
    }
}
