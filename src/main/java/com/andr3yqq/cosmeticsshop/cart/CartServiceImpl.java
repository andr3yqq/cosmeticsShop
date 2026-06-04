package com.andr3yqq.cosmeticsshop.cart;

import com.andr3yqq.cosmeticsshop.product.Product;
import com.andr3yqq.cosmeticsshop.product.ProductRepository;
import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Cart createCart(CartDTO cartDTO) {
        if (cartDTO == null || cartDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID must not be null when creating a cart.");
        }

        User user = userRepository.findById(cartDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + cartDTO.getUserId()));

        Optional<Cart> existingCart = cartRepository.findByUserId(cartDTO.getUserId());
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setCartItems(new ArrayList<>());

        Cart savedCart = cartRepository.save(cart);

        if (cartDTO.getCartItems() != null && !cartDTO.getCartItems().isEmpty()) {
            List<CartItem> items = new ArrayList<>();
            for (CartItemDTO itemDTO : cartDTO.getCartItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemDTO.getProductId()));
                CartItem item = new CartItem();
                item.setCart(savedCart);
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setMomentPrice(itemDTO.getMomentPrice() != null ? itemDTO.getMomentPrice() : product.getPrice());
                items.add(item);
            }
            savedCart.setCartItems(items);
            return cartRepository.save(savedCart);
        }

        return savedCart;
    }

    @Override
    @Transactional
    public Cart updateCart(CartDTO cartDTO) {
        if (cartDTO == null || cartDTO.getId() == null) {
            throw new IllegalArgumentException("Cart ID must not be null when updating a cart.");
        }

        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartDTO.getId()));

        cart.setUpdatedAt(LocalDateTime.now());

        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        } else {
            cart.getCartItems().clear();
        }

        if (cartDTO.getCartItems() != null) {
            for (CartItemDTO itemDTO : cartDTO.getCartItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemDTO.getProductId()));
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setMomentPrice(itemDTO.getMomentPrice() != null ? itemDTO.getMomentPrice() : product.getPrice());
                cart.getCartItems().add(item);
            }
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart clearCart(CartDTO cartDTO) {
        if (cartDTO == null || cartDTO.getId() == null) {
            throw new IllegalArgumentException("Cart ID must not be null when clearing a cart.");
        }

        Cart cart = cartRepository.findById(cartDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartDTO.getId()));

        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    @Override
    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + id));
    }

    @Override
    public CartItem getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + id));
    }

    @Override
    @Transactional
    public CartItem updateCartItem(CartItemDTO cartItemDTO) {
        if (cartItemDTO == null || cartItemDTO.getId() == null) {
            throw new IllegalArgumentException("Cart item ID must not be null when updating a cart item.");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + cartItemDTO.getId()));

        cartItem.setQuantity(cartItemDTO.getQuantity());
        if (cartItemDTO.getMomentPrice() != null) {
            cartItem.setMomentPrice(cartItemDTO.getMomentPrice());
        } else if (cartItem.getProduct() != null) {
            cartItem.setMomentPrice(cartItem.getProduct().getPrice());
        }

        if (cartItem.getCart() != null) {
            cartItem.getCart().setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cartItem.getCart());
        }

        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public CartItem deleteCartItemById(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with id: " + id));

        Cart cart = cartItem.getCart();
        if (cart != null && cart.getCartItems() != null) {
            cart.getCartItems().remove(cartItem);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }

        cartItemRepository.delete(cartItem);
        return cartItem;
    }

    @Override
    public Double getFullCartPrice(CartDTO cartDTO) {
        if (cartDTO == null || cartDTO.getCartItems() == null) {
            return 0.0;
        }

        return cartDTO.getCartItems().stream()
                .mapToDouble(item -> {
                    Double price = item.getMomentPrice();
                    if (price == null) {
                        Product product = productRepository.findById(item.getProductId()).orElse(null);
                        price = product != null ? product.getPrice() : 0.0;
                    }
                    return price * item.getQuantity();
                })
                .sum();
    }
}
