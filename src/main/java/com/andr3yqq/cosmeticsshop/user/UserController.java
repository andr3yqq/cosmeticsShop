package com.andr3yqq.cosmeticsshop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserDTO userDto) {
        try {
            User user = userService.createUser(userDto);
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Registration failed: User with email " + userDto.getEmail() + " already exists.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "Registration successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error during registration: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            User user = userService.loginUser(userLoginDTO);
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Login failed: Invalid email or password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "Login successful");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserDTO userDto) {
        try {
            User user = userService.updateUser(userDto);
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Update failed: User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "User updated successfully");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error during user update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "User retrieved successfully");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error retrieving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("User not found with email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "User retrieved successfully");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error retrieving user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponseDTO> responseDTOs = users.stream()
                    .map(user -> mapToResponseDTO(user, "User retrieved"))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserResponseDTO> resetPassword(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            User user = userService.getUserByEmail(userLoginDTO.getEmail());
            if (user == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Reset password failed: User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            userService.resetPassword(userLoginDTO);
            UserResponseDTO responseDTO = new UserResponseDTO();
            responseDTO.setEmail(userLoginDTO.getEmail());
            responseDTO.setMessage("Password reset successful.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error resetting password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}/role")
    public ResponseEntity<RoleDTO> getUserRole(@PathVariable Long id) {
        try {
            Role role = userService.getUserRole(id);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            RoleDTO roleDTO = new RoleDTO(role.getId(), role.getName(), role.getCreatedAt(), role.getUpdatedAt());
            return ResponseEntity.ok(roleDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDTO> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            UserResponseDTO responseDTO = new UserResponseDTO();
            responseDTO.setMessage("User deleted successfully.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private UserResponseDTO mapToResponseDTO(User user, String message) {
        if (user == null) return null;
        Long addressId = user.getAddress() != null ? user.getAddress().getId() : null;
        String roleName = user.getRole() != null ? user.getRole().getName() : null;

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                addressId,
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                roleName,
                user.isActive(),
                message
        );
    }
}
