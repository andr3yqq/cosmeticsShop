package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.brand.Brand;
import com.andr3yqq.cosmeticsshop.brand.BrandRepository;
import com.andr3yqq.cosmeticsshop.image.ImageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplImageTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTO;
    private Product product;
    private Brand brand;
    private ImageDTO imageDTO;

    @BeforeEach
    void setUp() {
        brand = new Brand(1L, "Loreal", "Loreal desc", null);
        imageDTO = new ImageDTO(1L, "http://example.com/img.jpg", "img", "jpg");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setSku("SKU-123");
        productDTO.setBrand("Loreal");
        productDTO.setName("Hydrating Cream");
        productDTO.setPrice(25.99);
        productDTO.setAvailableStock(100L);
        productDTO.setCategory("SKINCARE");
        productDTO.setStatus("IN_STOCK");
        productDTO.setFeatures(List.of("Organic"));
        productDTO.setImages(List.of(imageDTO));

        product = new Product(
                "SKU-123",
                "Loreal",
                "Hydrating Cream",
                "Deep hydrating cream for face",
                25.99,
                100L,
                ProductStatus.IN_STOCK,
                ProductCategory.SKINCARE
        );
        product.setId(1L);
        product.setImages(new ArrayList<>());
    }

    @Test
    void createProduct_BrandNotFound_ThrowsException() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(productDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_BrandFound_Success() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(brand));
        when(productRepository.findBySku("SKU-123")).thenReturn(null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals("Loreal", result.getBrand());
        assertEquals(1, result.getImages().size());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_BrandNotFound_ThrowsException() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_BrandFound_Success() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(brand));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(productDTO);

        assertNotNull(result);
        assertEquals("Loreal", result.getBrand());
        assertEquals(1, result.getImages().size());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
