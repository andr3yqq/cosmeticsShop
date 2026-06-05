package com.andr3yqq.cosmeticsshop.order;

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

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            orderDTO.setUserId(user.getId()); // Force authenticated user's ID
            Order order = orderService.createOrder(orderDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toOrderDTO(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            return ResponseEntity.ok(orderMapper.toOrderDTOList(orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            if (!isOwnerOrAdmin(user, order)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(orderMapper.toOrderDTO(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orderMapper.toOrderDTOList(orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDTO orderDTO) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRequiredRole(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Order existingOrder = orderService.getOrderById(id);
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            boolean isUserAdmin = isAdmin(user);
            boolean isOwner = existingOrder.getUser() != null && user.getId().equals(existingOrder.getUser().getId());

            if (!isUserAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Non-admin can only request to CANCEL their own PENDING order
            if (!isUserAdmin) {
                if (!"CANCELLED".equalsIgnoreCase(orderDTO.getStatus()) || existingOrder.getStatus() != OrderStatus.PENDING) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            orderDTO.setId(id);
            Order updatedOrder = orderService.updateOrder(orderDTO);
            return ResponseEntity.ok(orderMapper.toOrderDTO(updatedOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && "ROLE_ADMIN".equals(user.getRole().getName());
    }

    private boolean isOwnerOrAdmin(User user, Order order) {
        if (user == null || order == null) return false;
        if (isAdmin(user)) return true;
        return order.getUser() != null && user.getId().equals(order.getUser().getId());
    }
}
