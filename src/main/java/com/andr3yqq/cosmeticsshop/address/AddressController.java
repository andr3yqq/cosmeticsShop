package com.andr3yqq.cosmeticsshop.address;

import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;
    private final AddressMapper addressMapper;

    @GetMapping("/me")
    public ResponseEntity<AddressDTO> getMyAddress() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getAddress() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(addressMapper.toAddressDTO(user.getAddress()));
    }

    @PostMapping
    public ResponseEntity<AddressDTO> createMyAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Address savedAddress = addressService.createAddressForUser(user.getId(), addressDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressMapper.toAddressDTO(savedAddress));
    }

    @PutMapping
    public ResponseEntity<AddressDTO> updateMyAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Address updatedAddress = addressService.updateAddressForUser(user.getId(), addressDTO);
        return ResponseEntity.ok(addressMapper.toAddressDTO(updatedAddress));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyAddress() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        addressService.deleteAddressForUser(user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Address address = addressService.getAddressById(id);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }

        if (!isSelfOrAdmin(user, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(addressMapper.toAddressDTO(address));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) return null;

        return userService.getOrCreateUserFromJwt((Jwt) principal);
    }

    private boolean isSelfOrAdmin(User authenticatedUser, Long addressId) {
        boolean isAdmin = authenticatedUser.getRole() != null &&
                "ROLE_ADMIN".equals(authenticatedUser.getRole().getName());
        if (isAdmin) return true;

        return authenticatedUser.getAddress() != null &&
                authenticatedUser.getAddress().getId().equals(addressId);
    }
}
