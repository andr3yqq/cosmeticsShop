package com.andr3yqq.cosmeticsshop.user;

import com.andr3yqq.cosmeticsshop.address.Address;
import com.andr3yqq.cosmeticsshop.address.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;
    private Role role;
    private Address address;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "ROLE_USER", LocalDateTime.now(), LocalDateTime.now());
        address = new Address(2L, "123 Street", "Apt 1", "City", "State", "12345", "Country", LocalDateTime.now(), LocalDateTime.now());
        
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setPhoneNumber("123456789");
        userDTO.setRole("ROLE_USER");
        userDTO.setAddressId(2L);
        userDTO.setActive(true);

        user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(1L);
        user.setPhoneNumber("123456789");
        user.setRole(role);
        user.setAddress(address);
        user.setActive(true);
    }

    @Test
    void getOrCreateUserFromJwt_ExistingUser() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class, withSettings().strictness(Strictness.LENIENT));
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getOrCreateUserFromJwt_NewUser_Success() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class, withSettings().strictness(Strictness.LENIENT));
        when(jwt.getClaim("email")).thenReturn("new@example.com");
        when(jwt.getClaim("given_name")).thenReturn("Jane");
        when(jwt.getClaim("family_name")).thenReturn("Smith");
        when(userRepository.getUserByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("ROLE_USER", result.getRole().getName());
        assertEquals("EXTERNAL_AUTH0_MANAGED", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getOrCreateUserFromJwt_NewUser_FallbackName() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class, withSettings().strictness(Strictness.LENIENT));
        when(jwt.getClaim("email")).thenReturn("new@example.com");
        when(jwt.getClaim("given_name")).thenReturn(null);
        when(jwt.getClaim("family_name")).thenReturn(null);
        when(jwt.getClaim("name")).thenReturn("SingleName");
        when(userRepository.getUserByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("SingleName", result.getFirstName());
        assertEquals("User", result.getLastName());
    }

    @Test
    void getOrCreateUserFromJwt_NewUser_CustomClaimNamespaceWithSlash() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class, withSettings().strictness(Strictness.LENIENT));
        
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("https://cosmetics-shop.com/email", "custom@example.com");
        claims.put("https://cosmetics-shop.com/given_name", "Alex");
        claims.put("https://cosmetics-shop.com/family_name", "Smith");

        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.getClaim("https://cosmetics-shop.com/email")).thenReturn("custom@example.com");
        when(jwt.getClaim("https://cosmetics-shop.com/given_name")).thenReturn("Alex");
        when(jwt.getClaim("https://cosmetics-shop.com/family_name")).thenReturn("Smith");
        when(userRepository.getUserByEmail("custom@example.com")).thenReturn(Optional.empty());
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertNotNull(result);
        assertEquals("custom@example.com", result.getEmail());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    void getOrCreateUserFromJwt_NewUser_CustomClaimNamespaceWithoutSlash() {
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class, withSettings().strictness(Strictness.LENIENT));
        
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("https://cosmetics-shop.com/email", "custom@example.com");
        claims.put("https://cosmetics-shop.com/given_name", "Alex");
        claims.put("https://cosmetics-shop.com/family_name", "Smith");

        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.getClaim("https://cosmetics-shop.com/email")).thenReturn("custom@example.com");
        when(jwt.getClaim("https://cosmetics-shop.com/given_name")).thenReturn("Alex");
        when(jwt.getClaim("https://cosmetics-shop.com/family_name")).thenReturn("Smith");
        when(userRepository.getUserByEmail("custom@example.com")).thenReturn(Optional.empty());
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.getOrCreateUserFromJwt(jwt);

        assertNotNull(result);
        assertEquals("custom@example.com", result.getEmail());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userDTO);

        assertNotNull(updatedUser);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_Admin_Success_WithRoleUpdate() {
        // Mock authentication context for ADMIN user
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();
        SecurityContextHolder.setContext(securityContext);

        try {
            Role adminRole = new Role(2L, "ROLE_ADMIN", LocalDateTime.now(), LocalDateTime.now());
            userDTO.setRole("ROLE_ADMIN");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(roleRepository.getRoleByName("ROLE_ADMIN")).thenReturn(adminRole);
            when(addressRepository.findById(2L)).thenReturn(Optional.of(address));
            when(userRepository.save(any(User.class))).thenReturn(user);

            User updatedUser = userService.updateUser(userDTO);

            assertNotNull(updatedUser);
            verify(roleRepository, times(1)).getRoleByName("ROLE_ADMIN");
            verify(userRepository, times(1)).save(any(User.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void updateUser_Failure_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User updatedUser = userService.updateUser(userDTO);

        assertNull(updatedUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User foundUser = userService.getUserById(1L);

        assertNull(foundUser);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        User foundUser = userService.getUserByEmail("test@example.com");

        assertNull(foundUser);
    }

    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));

        Page<User> users = userService.getAllUsers(pageable);

        assertEquals(1, users.getContent().size());
        assertEquals("test@example.com", users.getContent().getFirst().getEmail());
    }



    @Test
    void getUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Role userRole = userService.getUserRole(1L);

        assertNotNull(userRole);
        assertEquals("ROLE_USER", userRole.getName());
    }

    @Test
    void getUserRole_Failure_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Role userRole = userService.getUserRole(1L);

        assertNull(userRole);
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
