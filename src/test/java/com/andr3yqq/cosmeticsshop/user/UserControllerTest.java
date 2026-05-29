package com.andr3yqq.cosmeticsshop.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;
    private UserLoginDTO loginDTO;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "ROLE_USER", LocalDateTime.now(), LocalDateTime.now());
        
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setPhoneNumber("123456789");
        userDTO.setRole("ROLE_USER");
        userDTO.setAddressId(null);
        userDTO.setActive(true);

        loginDTO = new UserLoginDTO("test@example.com", "password123");

        user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(1L);
        user.setPhoneNumber("123456789");
        user.setRole(role);
        user.setActive(true);

        // Stub getUserById for default BOLA security context checks in tests
        when(userService.getUserById(1L)).thenReturn(user);

        // Mock security context for a standard ROLE_USER named test@example.com
        Authentication authentication =
                mock(Authentication.class);
        SecurityContext securityContext =
                mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void register_Success() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void register_AlreadyExists() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void register_Exception() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(containsString("Database down")));
    }

    @Test
    void login_Success() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_Failure_InvalidCredentials() throws Exception {
        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));
    }

    @Test
    void updateUser_Success() throws Exception {
        when(userService.updateUser(any(UserDTO.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        when(userService.updateUser(any(UserDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        Authentication authentication =
                mock(Authentication.class);
        SecurityContext securityContext =
                mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        try {
            when(userService.getAllUsers()).thenReturn(List.of(user));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value("test@example.com"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void resetPassword_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        doNothing().when(userService).resetPassword(any(UserLoginDTO.class));

        mockMvc.perform(post("/api/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful."));
    }

    @Test
    void resetPassword_NotFound() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(null);

        mockMvc.perform(post("/api/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void getUserRole_Success() throws Exception {
        when(userService.getUserRole(1L)).thenReturn(role);

        mockMvc.perform(get("/api/users/1/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ROLE_USER"));
    }

    @Test
    void getUserRole_NotFound() throws Exception {
        when(userService.getUserRole(1L)).thenReturn(null);

        mockMvc.perform(get("/api/users/1/role"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully."));
    }
}
