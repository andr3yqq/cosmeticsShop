package com.andr3yqq.cosmeticsshop.product;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(
    controllers = ProductController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;
    private Product product;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setSku("SKU-123");
        productDTO.setBrand("Loreal");
        productDTO.setName("Hydrating Cream");
        productDTO.setDescription("Deep hydrating cream");
        productDTO.setPrice(25.99);
        productDTO.setAvailableStock(100L);
        productDTO.setCategory("SKINCARE");
        productDTO.setStatus("IN_STOCK");

        product = new Product(
                "SKU-123",
                "Loreal",
                "Hydrating Cream",
                "Deep hydrating cream",
                25.99,
                100L,
                ProductStatus.IN_STOCK,
                ProductCategory.SKINCARE
        );
        product.setId(1L);
    }

    @Test
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(post("/api/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-123"))
                .andExpect(jsonPath("$.name").value("Hydrating Cream"));
    }

    @Test
    void createProduct_Failure() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(null);
        when(productMapper.toProductDTO(null)).thenReturn(null);

        mockMvc.perform(post("/api/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sku").value("SKU-123"));
    }

    @Test
    void updateProduct_Success() throws Exception {
        when(productService.updateProduct(any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(put("/api/products/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-123"));
    }

    @Test
    void updateProduct_Failure() throws Exception {
        when(productService.updateProduct(any(ProductDTO.class))).thenReturn(null);
        when(productMapper.toProductDTO(null)).thenReturn(null);

        mockMvc.perform(put("/api/products/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sku").value("SKU-123"));
    }

    @Test
    void allProducts_Success() throws Exception {
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-123"));
    }

    @Test
    void allProductsByBrand_Success() throws Exception {
        when(productService.getProductsByBrand(eq("Loreal"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/brand/Loreal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].brand").value("Loreal"));
    }

    @Test
    void allProductsByCategory_Success() throws Exception {
        when(productService.getProductsByCategory(eq("SKINCARE"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/category/SKINCARE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("SKINCARE"));
    }

    @Test
    void allProductsByName_Success() throws Exception {
        when(productService.getProductsByName(eq("Hydrating Cream"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/name/Hydrating Cream"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Hydrating Cream"));
    }
}
