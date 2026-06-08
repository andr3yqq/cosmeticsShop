package com.andr3yqq.cosmeticsshop.order;

import com.andr3yqq.cosmeticsshop.cart.Cart;
import com.andr3yqq.cosmeticsshop.cart.CartItem;
import com.andr3yqq.cosmeticsshop.cart.CartRepository;
import com.andr3yqq.cosmeticsshop.product.Product;
import com.andr3yqq.cosmeticsshop.product.ProductRepository;
import com.andr3yqq.cosmeticsshop.product.ProductStatus;
import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null when creating an order.");
        }

        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + orderDTO.getUserId()));

        Cart cart = cartRepository.findByUserId(orderDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user id: " + orderDTO.getUserId()));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place order: Cart is empty.");
        }

        if (orderDTO.getPaymentMethod() == null || orderDTO.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(orderDTO.getPaymentMethod());

        OrderStatus status = OrderStatus.PENDING;
        if (orderDTO.getStatus() != null) {
            try {
                status = OrderStatus.valueOf(orderDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default PENDING
            }
        }
        order.setStatus(status);

        String orderNumber = orderDTO.getOrderNumber();
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        order.setOrderNumber(orderNumber);

        double totalPrice = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Product not found for cart item.");
            }

            if (product.getStatus() == ProductStatus.DELISTED) {
                throw new IllegalArgumentException("Product is delisted: " + product.getName());
            }

            if (product.getAvailableStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                        ". Available stock: " + product.getAvailableStock() + ", requested: " + cartItem.getQuantity());
            }

            // Decrement stock
            product.setAvailableStock(product.getAvailableStock() - cartItem.getQuantity());
            if (product.getAvailableStock() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            double itemPrice = cartItem.getMomentPrice() != null ? cartItem.getMomentPrice() : product.getPrice();
            orderItem.setPricePerItem(itemPrice);
            orderItems.add(orderItem);

            totalPrice += itemPrice * cartItem.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(OrderDTO orderDTO) {
        if (orderDTO == null || orderDTO.getId() == null) {
            throw new IllegalArgumentException("Order ID must not be null when updating an order.");
        }

        Order order = orderRepository.findById(orderDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderDTO.getId()));

        if (orderDTO.getStatus() != null) {
            try {
                OrderStatus newStatus = OrderStatus.valueOf(orderDTO.getStatus().toUpperCase());
                if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
                    if (order.getOrderItems() != null) {
                        for (OrderItem orderItem : order.getOrderItems()) {
                            Product product = orderItem.getProduct();
                            if (product != null) {
                                product.setAvailableStock(product.getAvailableStock() + orderItem.getQuantity());
                                if (product.getStatus() == ProductStatus.OUT_OF_STOCK && product.getAvailableStock() > 0) {
                                    product.setStatus(ProductStatus.IN_STOCK);
                                }
                                productRepository.save(product);
                            }
                        }
                    }
                }
                order.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + orderDTO.getStatus());
            }
        }

        if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod(orderDTO.getPaymentMethod());
        }

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null.");
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }

    @Override
    public Page<Order> getOrdersByUserId(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Double getFullOrderPrice(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return 0.0;
        }
        if (orderDTO.getTotalPrice() != null) {
            return orderDTO.getTotalPrice();
        }
        if (orderDTO.getOrderItems() == null) {
            return 0.0;
        }
        return orderDTO.getOrderItems().stream()
                .mapToDouble(item -> {
                    Double price = item.getPricePerItem();
                    if (price == null && item.getProductId() != null) {
                        Product product = productRepository.findById(item.getProductId()).orElse(null);
                        price = product != null ? product.getPrice() : 0.0;
                    }
                    return (price != null ? price : 0.0) * item.getQuantity();
                })
                .sum();
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
