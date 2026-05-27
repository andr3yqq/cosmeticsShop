package com.andr3yqq.cosmeticsshop.user;

import com.andr3yqq.cosmeticsshop.address.Address;
import com.andr3yqq.cosmeticsshop.address.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

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
    void createUser_Success_RoleAndAddressExist() {
        when(userRepository.getUserByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals(role, createdUser.getRole());
        assertEquals(address, createdUser.getAddress());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_Success_RoleDoesNotExist() {
        when(userRepository.getUserByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(null);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(userDTO);

        assertNotNull(createdUser);
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_Failure_UserAlreadyExists() {
        when(userRepository.getUserByEmail(userDTO.getEmail())).thenReturn(Optional.of(user));

        User createdUser = userService.createUser(userDTO);

        assertNull(createdUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "password123");
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User loggedInUser = userService.loginUser(loginDTO);

        assertNotNull(loggedInUser);
        assertEquals("test@example.com", loggedInUser.getEmail());
    }

    @Test
    void loginUser_Failure_WrongPassword() {
        UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "wrongPassword");
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        User loggedInUser = userService.loginUser(loginDTO);

        assertNull(loggedInUser);
    }

    @Test
    void loginUser_Failure_UserNotFound() {
        UserLoginDTO loginDTO = new UserLoginDTO("unknown@example.com", "password123");
        when(userRepository.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        User loggedInUser = userService.loginUser(loginDTO);

        assertNull(loggedInUser);
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.getRoleByName("ROLE_USER")).thenReturn(role);
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userDTO);

        assertNotNull(updatedUser);
        verify(userRepository, times(1)).save(any(User.class));
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
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("test@example.com", users.get(0).getEmail());
    }

    @Test
    void resetPassword_Success() {
        UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "newPassword123");
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        userService.resetPassword(loginDTO);

        verify(userRepository, times(1)).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
    }

    @Test
    void resetPassword_Failure_UserNotFound() {
        UserLoginDTO loginDTO = new UserLoginDTO("test@example.com", "newPassword123");
        when(userRepository.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        userService.resetPassword(loginDTO);

        verify(userRepository, never()).save(any(User.class));
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
