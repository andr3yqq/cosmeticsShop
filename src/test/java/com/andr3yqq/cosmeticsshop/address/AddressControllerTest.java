package com.andr3yqq.cosmeticsshop.address;

import com.andr3yqq.cosmeticsshop.config.SecurityConfig;
import com.andr3yqq.cosmeticsshop.user.Role;
import com.andr3yqq.cosmeticsshop.user.User;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AddressController.class)
@Import(SecurityConfig.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AddressMapper addressMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private AddressDTO addressDTO;
    private Address address;
    private User user;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, "ROLE_USER", LocalDateTime.now(), LocalDateTime.now());
        adminRole = new Role(2L, "ROLE_ADMIN", LocalDateTime.now(), LocalDateTime.now());

        addressDTO = new AddressDTO(1L, "123 Street", "Apt 1", "City", "State", "12345", "Country", LocalDateTime.now(), LocalDateTime.now());
        address = new Address(1L, "123 Street", "Apt 1", "City", "State", "12345", "Country", LocalDateTime.now(), LocalDateTime.now());

        user = new User("test@example.com", "password", "John", "Doe");
        user.setId(2L);
        user.setRole(userRole);
    }

    @Test
    void getMyAddress_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/addresses/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyAddress_Success() throws Exception {
        user.setAddress(address);
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        mockMvc.perform(get("/api/addresses/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getMyAddress_NoAddress_ReturnsNotFound() throws Exception {
        user.setAddress(null);
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);

        mockMvc.perform(get("/api/addresses/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMyAddress_Success() throws Exception {
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        when(addressService.createAddressForUser(eq(user.getId()), any(AddressDTO.class))).thenReturn(address);
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        mockMvc.perform(post("/api/addresses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createMyAddress_InvalidBody_ReturnsBadRequest() throws Exception {
        AddressDTO invalidDTO = new AddressDTO(); // blank fields should fail validation
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);

        mockMvc.perform(post("/api/addresses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMyAddress_Success() throws Exception {
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        when(addressService.updateAddressForUser(eq(user.getId()), any(AddressDTO.class))).thenReturn(address);
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        mockMvc.perform(put("/api/addresses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMyAddress_Success() throws Exception {
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        doNothing().when(addressService).deleteAddressForUser(user.getId());

        mockMvc.perform(delete("/api/addresses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAddressById_Owner_Success() throws Exception {
        user.setAddress(address);
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        when(addressService.getAddressById(1L)).thenReturn(address);
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        mockMvc.perform(get("/api/addresses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAddressById_Admin_Success() throws Exception {
        User adminUser = new User("admin@example.com", "password", "Admin", "User");
        adminUser.setId(3L);
        adminUser.setRole(adminRole);

        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(adminUser);
        when(addressService.getAddressById(1L)).thenReturn(address);
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        mockMvc.perform(get("/api/addresses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void getAddressById_NonOwnerUser_ReturnsForbidden() throws Exception {
        User otherUser = new User("other@example.com", "password", "Other", "User");
        otherUser.setId(4L);
        otherUser.setRole(userRole);
        otherUser.setAddress(null); // not the owner of address 1

        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(otherUser);
        when(addressService.getAddressById(1L)).thenReturn(address);

        mockMvc.perform(get("/api/addresses/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAddressById_NotFound_ReturnsNotFound() throws Exception {
        when(userService.getOrCreateUserFromJwt(any(Jwt.class))).thenReturn(user);
        when(addressService.getAddressById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/addresses/999")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }
}
