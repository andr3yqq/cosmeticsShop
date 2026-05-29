package com.andr3yqq.cosmeticsshop.product;

import org.junit.jupiter.api.Assertions;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import com.andr3yqq.cosmeticsshop.config.SecurityConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

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
    void jwtAuthenticationConverter_ExtractsStandardRoles() {
        var converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN"))
                .claim("scope", "read:products")
                .build();

        var token = converter.convert(jwt);
        var authorities = token.getAuthorities();

        boolean hasRoleAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasScopeRead = authorities.stream().anyMatch(a -> a.getAuthority().equals("SCOPE_read:products"));

        Assertions.assertTrue(hasRoleAdmin);
        Assertions.assertTrue(hasScopeRead);
    }

    @Test
    void jwtAuthenticationConverter_ExtractsCustomNamespaceRoles() {
        var converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("https://cosmetics-shop.com/roles", "ADMIN")
                .build();

        var token = converter.convert(jwt);
        var authorities = token.getAuthorities();

        boolean hasRoleAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Assertions.assertTrue(hasRoleAdmin);
    }

    @Test
    void jwtAuthenticationConverter_ExtractsPermissions() {
        var converter = securityConfig.jwtAuthenticationConverter();
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("permissions", List.of("ADMIN"))
                .build();

        var token = converter.convert(jwt);
        var authorities = token.getAuthorities();

        boolean hasRoleAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Assertions.assertTrue(hasRoleAdmin);
    }

    @Test
    void getProducts_PublicAccess_Success() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(productMapper.toProductDTOList(any())).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void createProduct_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/products/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/products/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_AdminRole_Success() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(post("/api/products/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_Anonymous_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/products/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/products/update")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_AdminRole_Success() throws Exception {
        when(productService.updateProduct(any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(productDTO);

        mockMvc.perform(put("/api/products/update")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk());
    }
}
