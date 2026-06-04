package com.andr3yqq.cosmeticsshop.cart;

import com.andr3yqq.cosmeticsshop.product.Product;
import com.andr3yqq.cosmeticsshop.product.ProductRepository;
import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;
    private CartDTO cartDTO;
    private CartItemDTO cartItemDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        product = new Product();
        product.setId(10L);
        product.setPrice(99.99);

        cartItem = new CartItem();
        cartItem.setId(20L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setMomentPrice(99.99);

        cart.getCartItems().add(cartItem);

        cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(20L);
        cartItemDTO.setProductId(10L);
        cartItemDTO.setQuantity(2);
        cartItemDTO.setMomentPrice(99.99);

        cartDTO = new CartDTO();
        cartDTO.setId(1L);
        cartDTO.setUserId(1L);
        cartDTO.setCartItems(List.of(cartItemDTO));
    }

    @Test
    void createCart_NewCart_Success() {
        CartDTO newCartDTO = new CartDTO();
        newCartDTO.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.createCart(newCartDTO);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void createCart_ExistingCart_ReturnsExisting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.createCart(cartDTO);

        assertNotNull(result);
        assertEquals(cart.getId(), result.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void createCart_UserNotFound_ThrowsException() {
        CartDTO newCartDTO = new CartDTO();
        newCartDTO.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cartService.createCart(newCartDTO));
    }

    @Test
    void updateCart_Success() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.updateCart(cartDTO);

        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCart_CartNotFound_ThrowsException() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cartService.updateCart(cartDTO));
    }

    @Test
    void clearCart_Success() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.clearCart(cartDTO);

        assertNotNull(result);
        assertTrue(result.getCartItems().isEmpty());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartById_Success() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCartById_NotFound_ThrowsException() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cartService.getCartById(1L));
    }

    @Test
    void getCartItemById_Success() {
        when(cartItemRepository.findById(20L)).thenReturn(Optional.of(cartItem));

        CartItem result = cartService.getCartItemById(20L);

        assertNotNull(result);
        assertEquals(20L, result.getId());
    }

    @Test
    void updateCartItem_Success() {
        when(cartItemRepository.findById(20L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartService.updateCartItem(cartItemDTO);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void deleteCartItemById_Success() {
        when(cartItemRepository.findById(20L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));

        CartItem result = cartService.deleteCartItemById(20L);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void getFullCartPrice_Success() {
        Double price = cartService.getFullCartPrice(cartDTO);

        assertNotNull(price);
        assertEquals(199.98, price, 0.001);
    }
}
