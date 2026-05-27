package com.andr3yqq.cosmeticsshop.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productService;
    private final ProductMapper productMapper;

    @PostMapping("/create")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productMapper.toProductDTO(productService.createProduct(productDTO));
        if (createdProduct == null)
            return ResponseEntity.badRequest().body(productDTO);
        return ResponseEntity.ok().body(createdProduct);
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productMapper.toProductDTO(productService.updateProduct(productDTO));
        if (updatedProduct == null)
            return ResponseEntity.badRequest().body(productDTO);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> allProducts() {
        List<ProductDTO> products = productMapper.toProductDTOList(productService.getAllProducts());
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductDTO>> allProductsByBrand(@PathVariable String brand) {
        List<ProductDTO> products = productMapper.toProductDTOList(productService.getProductsByBrand(brand));
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> allProductsByCategory(@PathVariable String category) {
        List<ProductDTO> products = productMapper.toProductDTOList(productService.getProductsByCategory(category));
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<ProductDTO>> allProductsByName(@PathVariable String name) {
        List<ProductDTO> products = productMapper.toProductDTOList(productService.getProductsByName(name));
        return ResponseEntity.ok().body(products);
    }
}
