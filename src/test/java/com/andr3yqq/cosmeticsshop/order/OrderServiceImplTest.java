package com.andr3yqq.cosmeticsshop.order;

import com.andr3yqq.cosmeticsshop.cart.Cart;
import com.andr3yqq.cosmeticsshop.cart.CartItem;
import com.andr3yqq.cosmeticsshop.cart.CartRepository;
import com.andr3yqq.cosmeticsshop.product.Product;
import com.andr3yqq.cosmeticsshop.product.ProductRepository;
import com.andr3yqq.cosmeticsshop.product.ProductStatus;
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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        product = new Product();
        product.setId(10L);
        product.setName("Moisturizer");
        product.setPrice(20.0);
        product.setAvailableStock(10L);
        product.setStatus(ProductStatus.IN_STOCK);

        cart = new Cart();
        cart.setId(100L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(200L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setMomentPrice(20.0);

        cart.getCartItems().add(cartItem);

        orderDTO = new OrderDTO();
        orderDTO.setUserId(1L);
        orderDTO.setPaymentMethod("CREDIT_CARD");
    }

    @Test
    void createOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(orderDTO);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(40.0, result.getTotalPrice());
        assertEquals(8L, product.getAvailableStock()); // Stock decremented from 10 to 8
        assertTrue(cart.getCartItems().isEmpty()); // Cart cleared

        verify(userRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void createOrder_EmptyCart_ThrowsException() {
        cart.getCartItems().clear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderDTO));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        cartItem.setQuantity(15); // requests 15, stock is 10

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderDTO));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_DelistedProduct_ThrowsException() {
        product.setStatus(ProductStatus.DELISTED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderDTO));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_StockBecomesZero_UpdatesStatusToOutOfStock() {
        cartItem.setQuantity(10); // requests all 10 items in stock

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });

        Order result = orderService.createOrder(orderDTO);

        assertNotNull(result);
        assertEquals(0L, product.getAvailableStock());
        assertEquals(ProductStatus.OUT_OF_STOCK, product.getStatus());
    }

    @Test
    void updateOrder_Success() {
        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setStatus(OrderStatus.PENDING);
        existingOrder.setPaymentMethod("CASH");

        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setId(1L);
        updateDTO.setStatus("PAID");
        updateDTO.setPaymentMethod("CREDIT_CARD");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrder(updateDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
    }

    @Test
    void updateOrder_CancelOrder_RestoresStock() {
        Product prod = new Product();
        prod.setId(10L);
        prod.setAvailableStock(5L);
        prod.setStatus(ProductStatus.OUT_OF_STOCK);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(200L);
        orderItem.setProduct(prod);
        orderItem.setQuantity(3);

        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setStatus(OrderStatus.PENDING);
        existingOrder.setOrderItems(List.of(orderItem));

        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setId(1L);
        updateDTO.setStatus("CANCELLED");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrder(updateDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(8L, prod.getAvailableStock()); // Stock increased by 3 (from 5 to 8)
        assertEquals(ProductStatus.IN_STOCK, prod.getStatus()); // Status changed back to IN_STOCK

        verify(productRepository, times(1)).save(prod);
    }

    @Test
    void getOrderById_Success() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrdersByUserId_Success() {
        Order order = new Order();
        order.setId(1L);
        List<Order> orders = List.of(order);

        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void getFullOrderPrice_Success() {
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setQuantity(3);
        itemDTO.setPricePerItem(15.0);

        OrderDTO dto = new OrderDTO();
        dto.setOrderItems(List.of(itemDTO));

        Double result = orderService.getFullOrderPrice(dto);

        assertEquals(45.0, result);
    }
}
