package com.andr3yqq.cosmeticsshop.cart;

import com.andr3yqq.cosmeticsshop.user.Role;
import com.andr3yqq.cosmeticsshop.user.User;
import com.andr3yqq.cosmeticsshop.user.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = CartController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CartMapper cartMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Role role;
    private Cart cart;
    private CartItem cartItem;
    private CartDTO cartDTO;
    private CartItemDTO cartItemDTO;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "ROLE_USER", LocalDateTime.now(), LocalDateTime.now());

        user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(1L);
        user.setRole(role);
        user.setActive(true);

        cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(20L);
        cartItem.setCart(cart);
        cartItem.setQuantity(2);
        cartItem.setMomentPrice(10.0);

        cart.getCartItems().add(cartItem);

        cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(20L);
        cartItemDTO.setProductId(5L);
        cartItemDTO.setQuantity(2);
        cartItemDTO.setMomentPrice(10.0);

        cartDTO = new CartDTO();
        cartDTO.setId(10L);
        cartDTO.setUserId(1L);
        cartDTO.setCartItems(List.of(cartItemDTO));

        // Mock security context for test@example.com
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwtMock = mock(Jwt.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtMock);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        SecurityContextHolder.setContext(securityContext);

        // Stub user service retrieval
        when(userService.getOrCreateUserFromJwt(any())).thenReturn(user);
    }

    @Test
    void getMyCart_Success() throws Exception {
        when(cartService.createCart(any(CartDTO.class))).thenReturn(cart);
        when(cartMapper.toCartDTO(any(Cart.class))).thenReturn(cartDTO);

        mockMvc.perform(get("/api/carts/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void updateMyCart_Success() throws Exception {
        when(cartService.getCartById(10L)).thenReturn(cart);
        when(cartService.updateCart(any(CartDTO.class))).thenReturn(cart);
        when(cartMapper.toCartDTO(any(Cart.class))).thenReturn(cartDTO);

        mockMvc.perform(put("/api/carts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void updateMyCart_Forbidden_NotOwner() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(99L);

        Cart foreignCart = new Cart();
        foreignCart.setId(10L);
        foreignCart.setUser(anotherUser);

        when(cartService.getCartById(10L)).thenReturn(foreignCart);

        mockMvc.perform(put("/api/carts/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clearMyCart_Success() throws Exception {
        when(cartService.getCartById(10L)).thenReturn(cart);
        when(cartService.clearCart(any(CartDTO.class))).thenReturn(cart);
        when(cartMapper.toCartDTO(any(Cart.class))).thenReturn(cartDTO);

        mockMvc.perform(post("/api/carts/me/clear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCartItem_Success() throws Exception {
        when(cartService.getCartItemById(20L)).thenReturn(cartItem);
        when(cartService.updateCartItem(any(CartItemDTO.class))).thenReturn(cartItem);
        when(cartMapper.toCartItemDTO(any(CartItem.class))).thenReturn(cartItemDTO);

        mockMvc.perform(put("/api/carts/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
    void deleteCartItem_Success() throws Exception {
        when(cartService.getCartItemById(20L)).thenReturn(cartItem);
        when(cartService.deleteCartItemById(20L)).thenReturn(cartItem);
        when(cartMapper.toCartItemDTO(any(CartItem.class))).thenReturn(cartItemDTO);

        mockMvc.perform(delete("/api/carts/items/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
    void getFullCartPrice_Success() throws Exception {
        when(cartService.getFullCartPrice(any(CartDTO.class))).thenReturn(20.0);

        mockMvc.perform(post("/api/carts/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(20.0));
    }
}
