package com.andr3yqq.cosmeticsshop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof Jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Jwt jwt = (Jwt) principal;
            User user = userService.getOrCreateUserFromJwt(jwt);
            UserResponseDTO userResponseDTO = mapToResponseDTO(user, "User profile retrieved successfully");
            return ResponseEntity.ok(userResponseDTO);
        } catch (Exception e) {
            UserResponseDTO errorResponse = new UserResponseDTO();
            errorResponse.setMessage("Error retrieving active profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserDTO userDto) {
        try {
            User existingUser = userService.getUserById(userDto.getId());
            if (existingUser == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Update failed: User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            if (!isSelfOrAdmin(existingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
            if (!isSelfOrAdmin(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
            if (!isSelfOrAdmin(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
            if (!isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            List<User> users = userService.getAllUsers();
            List<UserResponseDTO> responseDTOs = users.stream()
                    .map(user -> mapToResponseDTO(user, "User retrieved"))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/{id}/role")
    public ResponseEntity<RoleDTO> getUserRole(@PathVariable Long id) {
        try {
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (!isSelfOrAdmin(existingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                UserResponseDTO errorResponse = new UserResponseDTO();
                errorResponse.setMessage("Delete failed: User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            if (!isSelfOrAdmin(existingUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isSelfOrAdmin(User user) {
        if (user == null) return false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt) {
            try {
                User activeUser = userService.getOrCreateUserFromJwt((Jwt) principal);
                if (activeUser != null && activeUser.getId().equals(user.getId())) {
                    return true;
                }
            } catch (Exception e) {
                // Fall back
            }
        }

        String currentEmail = authentication.getName();
        return currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail());
    }

    private boolean isSelfOrAdmin(String email) {
        if (email == null) return false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt) {
            try {
                User activeUser = userService.getOrCreateUserFromJwt((Jwt) principal);
                if (activeUser != null && activeUser.getEmail().equalsIgnoreCase(email)) {
                    return true;
                }
            } catch (Exception e) {
                // Fall back
            }
        }

        String currentEmail = authentication.getName();
        return currentEmail != null && currentEmail.equalsIgnoreCase(email);
    }
}
