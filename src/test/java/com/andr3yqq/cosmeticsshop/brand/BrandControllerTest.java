package com.andr3yqq.cosmeticsshop.brand;

import com.andr3yqq.cosmeticsshop.config.SecurityConfig;
import com.andr3yqq.cosmeticsshop.user.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BrandController.class)
@Import(SecurityConfig.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private BrandMapper brandMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private BrandDTO brandDTO;
    private Brand brand;

    @BeforeEach
    void setUp() {
        brandDTO = new BrandDTO(1L, "Loreal", "Loreal description", 1L);
        brand = new Brand(1L, "Loreal", "Loreal description", null);
    }

    @Test
    void getAllBrands_Success() throws Exception {
        when(brandService.getAllBrands()).thenReturn(List.of(brand));
        when(brandMapper.toBrandDTOList(any())).thenReturn(List.of(brandDTO));

        mockMvc.perform(get("/api/brands")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getBrandById_Success() throws Exception {
        when(brandService.getBrandById(1L)).thenReturn(brand);
        when(brandMapper.toBrandDTO(brand)).thenReturn(brandDTO);

        mockMvc.perform(get("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getBrandById_NotFound_ReturnsNotFound() throws Exception {
        when(brandService.getBrandById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBrand_Admin_Success() throws Exception {
        when(brandService.createBrand(any(BrandDTO.class))).thenReturn(brand);
        when(brandMapper.toBrandDTO(brand)).thenReturn(brandDTO);

        mockMvc.perform(post("/api/brands")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createBrand_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/brands")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBrand_Admin_Success() throws Exception {
        when(brandService.updateBrand(any(BrandDTO.class))).thenReturn(brand);
        when(brandMapper.toBrandDTO(brand)).thenReturn(brandDTO);

        mockMvc.perform(put("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateBrand_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBrand_Admin_Success() throws Exception {
        doNothing().when(brandService).deleteBrand(1L);

        mockMvc.perform(delete("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBrand_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/brands/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}
