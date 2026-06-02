package com.andr3yqq.cosmeticsshop.image;

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

@WebMvcTest(controllers = ImageController.class)
@Import(SecurityConfig.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private ImageMapper imageMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private ImageDTO imageDTO;
    private Image image;

    @BeforeEach
    void setUp() {
        imageDTO = new ImageDTO(1L, "http://example.com/image.jpg", "product-image", "jpg");
        image = new Image(1L, "http://example.com/image.jpg", "product-image", "jpg");
    }

    @Test
    void getAllImages_Success() throws Exception {
        when(imageService.getAllImages()).thenReturn(List.of(image));
        when(imageMapper.toImageDTOList(any())).thenReturn(List.of(imageDTO));

        mockMvc.perform(get("/api/images")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getImageById_Success() throws Exception {
        when(imageService.getImageById(1L)).thenReturn(image);
        when(imageMapper.toImageDTO(image)).thenReturn(imageDTO);

        mockMvc.perform(get("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getImageById_NotFound_ReturnsNotFound() throws Exception {
        when(imageService.getImageById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createImage_Admin_Success() throws Exception {
        when(imageService.createImage(any(ImageDTO.class))).thenReturn(image);
        when(imageMapper.toImageDTO(image)).thenReturn(imageDTO);

        mockMvc.perform(post("/api/images")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createImage_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/images")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateImage_Admin_Success() throws Exception {
        when(imageService.updateImage(any(ImageDTO.class))).thenReturn(image);
        when(imageMapper.toImageDTO(image)).thenReturn(imageDTO);

        mockMvc.perform(put("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateImage_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteImage_Admin_Success() throws Exception {
        doNothing().when(imageService).deleteImage(1L);

        mockMvc.perform(delete("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteImage_User_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/images/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}
