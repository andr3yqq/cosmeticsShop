package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.brand.Brand;
import com.andr3yqq.cosmeticsshop.brand.BrandRepository;
import com.andr3yqq.cosmeticsshop.image.Image;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTO;
    private Product product;
    private ImageDTO imageDTO;

    @BeforeEach
    void setUp() {
        imageDTO = new ImageDTO(1L, "http://example.com/image.jpg", "main", "jpg");
        
        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setSku("SKU-123");
        productDTO.setBrand("Loreal");
        productDTO.setName("Hydrating Cream");
        productDTO.setDescription("Deep hydrating cream for face");
        productDTO.setPrice(25.99);
        productDTO.setLastPrice(25.99);
        productDTO.setAvailableStock(100L);
        productDTO.setCategory("SKINCARE");
        productDTO.setStatus("IN_STOCK");
        productDTO.setFeatures(List.of("Organic", "Fragrance-free"));
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
        product.setFeatures(List.of("Organic", "Fragrance-free"));
        
        Image image = new Image(1L, "http://example.com/image.jpg", "main", "jpg");
        image.setProduct(product);
        product.setImages(new ArrayList<>(List.of(image)));
    }

    @Test
    void createProduct_Success() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(new Brand()));
        when(productRepository.findBySku("SKU-123")).thenReturn(null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Product createdProduct = productService.createProduct(productDTO);

        assertNotNull(createdProduct);
        assertEquals(1L, createdProduct.getId());
        assertEquals("SKU-123", createdProduct.getSku());
        assertEquals(ProductStatus.IN_STOCK, createdProduct.getStatus());
        assertEquals(ProductCategory.SKINCARE, createdProduct.getCategory());
        assertEquals(2, createdProduct.getFeatures().size());
        assertEquals(1, createdProduct.getImages().size());
        assertEquals(createdProduct, createdProduct.getImages().getFirst().getProduct());

        verify(productRepository, times(1)).findBySku("SKU-123");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_Failure_DuplicateSku() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(new Brand()));
        when(productRepository.findBySku("SKU-123")).thenReturn(product);

        Product createdProduct = productService.createProduct(productDTO);

        assertNull(createdProduct);
        verify(productRepository, times(1)).findBySku("SKU-123");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_Success() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(new Brand()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productDTO.setPrice(29.99); // Price change
        productDTO.setFeatures(List.of("Organic", "Fragrance-free", "Sensitive Skin"));

        Product updatedProduct = productService.updateProduct(productDTO);

        assertNotNull(updatedProduct);
        assertEquals(29.99, updatedProduct.getPrice());
        assertEquals(25.99, updatedProduct.getLastPrice()); // Verify last price updated
        assertEquals(3, updatedProduct.getFeatures().size());
        assertEquals(1, updatedProduct.getImages().size());
        assertEquals(updatedProduct, updatedProduct.getImages().getFirst().getProduct());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_Failure_NotFound() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(new Brand()));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Product updatedProduct = productService.updateProduct(productDTO);

        assertNull(updatedProduct);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_Success() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("SKU-123", products.getFirst().getSku());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product foundProduct = productService.getProductById(1L);

        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.getId());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Product foundProduct = productService.getProductById(1L);

        assertNull(foundProduct);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductBySku_Success() {
        when(productRepository.findBySku("SKU-123")).thenReturn(product);

        Product foundProduct = productService.getProductBySku("SKU-123");

        assertNotNull(foundProduct);
        assertEquals("SKU-123", foundProduct.getSku());
        verify(productRepository, times(1)).findBySku("SKU-123");
    }

    @Test
    void getProductsByBrand_Success() {
        when(productRepository.findAllByBrand("Loreal")).thenReturn(List.of(product));

        List<Product> products = productService.getProductsByBrand("Loreal");

        assertEquals(1, products.size());
        assertEquals("Loreal", products.getFirst().getBrand());
        verify(productRepository, times(1)).findAllByBrand("Loreal");
    }

    @Test
    void getProductsByCategory_Success() {
        when(productRepository.findAllByCategory(ProductCategory.SKINCARE)).thenReturn(List.of(product));

        List<Product> products = productService.getProductsByCategory("SKINCARE");

        assertEquals(1, products.size());
        assertEquals(ProductCategory.SKINCARE, products.getFirst().getCategory());
        verify(productRepository, times(1)).findAllByCategory(ProductCategory.SKINCARE);
    }

    @Test
    void getProductsByName_Success() {
        when(productRepository.findAllByName("Hydrating Cream")).thenReturn(List.of(product));

        List<Product> products = productService.getProductsByName("Hydrating Cream");

        assertEquals(1, products.size());
        assertEquals("Hydrating Cream", products.getFirst().getName());
        verify(productRepository, times(1)).findAllByName("Hydrating Cream");
    }
}
