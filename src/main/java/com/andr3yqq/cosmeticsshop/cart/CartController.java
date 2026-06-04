package com.andr3yqq.cosmeticsshop.cart;

import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final CartMapper cartMapper;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartDTO> getMyCart() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // Get or create the cart for this user
            CartDTO initialDTO = new CartDTO();
            initialDTO.setUserId(user.getId());
            Cart cart = cartService.createCart(initialDTO);
            return ResponseEntity.ok(cartMapper.toCartDTO(cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartDTO> updateMyCart(@Valid @RequestBody CartDTO cartDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Cart existingCart = cartService.getCartById(cartDTO.getId());
            if (existingCart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Enforce ownership: Cart must belong to the authenticated user
            if (!isOwnerOrAdmin(user, existingCart)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            cartDTO.setUserId(user.getId()); // ensure they don't overwrite user ID to someone else
            Cart updatedCart = cartService.updateCart(cartDTO);
            return ResponseEntity.ok(cartMapper.toCartDTO(updatedCart));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/me/clear")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartDTO> clearMyCart(@RequestBody CartDTO cartDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Cart existingCart = cartService.getCartById(cartDTO.getId());
            if (existingCart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (!isOwnerOrAdmin(user, existingCart)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            cartDTO.setUserId(user.getId());
            Cart clearedCart = cartService.clearCart(cartDTO);
            return ResponseEntity.ok(cartMapper.toCartDTO(clearedCart));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/items")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartItemDTO> updateCartItem(@Valid @RequestBody CartItemDTO cartItemDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CartItem existingItem = cartService.getCartItemById(cartItemDTO.getId());
            if (existingItem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (!isOwnerOrAdmin(user, existingItem.getCart())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CartItem updatedItem = cartService.updateCartItem(cartItemDTO);
            return ResponseEntity.ok(cartMapper.toCartItemDTO(updatedItem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartItemDTO> deleteCartItem(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CartItem existingItem = cartService.getCartItemById(id);
            if (existingItem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (!isOwnerOrAdmin(user, existingItem.getCart())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            CartItem deletedItem = cartService.deleteCartItemById(id);
            return ResponseEntity.ok(cartMapper.toCartItemDTO(deletedItem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/price")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Double> getFullCartPrice(@RequestBody CartDTO cartDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (cartDTO.getId() != null) {
                Cart existingCart = cartService.getCartById(cartDTO.getId());
                if (existingCart != null && !isOwnerOrAdmin(user, existingCart)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            Double price = cartService.getFullCartPrice(cartDTO);
            return ResponseEntity.ok(price);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) return null;

        return userService.getOrCreateUserFromJwt((Jwt) principal);
    }

    private boolean hasRequiredRole(User user) {
        if (user == null || user.getRole() == null) return false;
        String roleName = user.getRole().getName();
        return "ROLE_USER".equals(roleName) || "ROLE_ADMIN".equals(roleName);
    }

    private boolean isOwnerOrAdmin(User user, Cart cart) {
        if (user == null || cart == null) return false;
        if ("ROLE_ADMIN".equals(user.getRole().getName())) return true;
        return cart.getUser() != null && user.getId().equals(cart.getUser().getId());
    }
}
