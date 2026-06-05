package com.andr3yqq.cosmeticsshop.order;

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
    controllers = OrderController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Role role;
    private User adminUser;
    private Role adminRole;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "ROLE_USER", LocalDateTime.now(), LocalDateTime.now());
        adminRole = new Role(2L, "ROLE_ADMIN", LocalDateTime.now(), LocalDateTime.now());

        user = new User("test@example.com", "encodedPassword", "John", "Doe");
        user.setId(1L);
        user.setRole(role);
        user.setActive(true);

        adminUser = new User("admin@example.com", "encodedPassword", "Admin", "User");
        adminUser.setId(2L);
        adminUser.setRole(adminRole);
        adminUser.setActive(true);

        order = new Order();
        order.setId(10L);
        order.setUser(user);
        order.setOrderNumber("ORD-12345");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(40.0);
        order.setPaymentMethod("CREDIT_CARD");
        order.setOrderItems(new ArrayList<>());

        orderDTO = new OrderDTO();
        orderDTO.setId(10L);
        orderDTO.setUserId(1L);
        orderDTO.setOrderNumber("ORD-12345");
        orderDTO.setStatus("PENDING");
        orderDTO.setTotalPrice(40.0);
        orderDTO.setPaymentMethod("CREDIT_CARD");
        orderDTO.setOrderItems(new ArrayList<>());

        // Default mock authentication for test@example.com
        mockSecurityContext(user);
    }

    private void mockSecurityContext(User activeUser) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwtMock = mock(Jwt.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(activeUser.getEmail());
        when(authentication.getPrincipal()).thenReturn(jwtMock);
        
        String roleName = activeUser.getRole().getName();
        doReturn(List.of(new SimpleGrantedAuthority(roleName))).when(authentication).getAuthorities();

        SecurityContextHolder.setContext(securityContext);

        when(userService.getOrCreateUserFromJwt(any())).thenReturn(activeUser);
    }

    @Test
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(order);
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    void createOrder_BadRequest() throws Exception {
        when(orderService.createOrder(any(OrderDTO.class))).thenThrow(new IllegalArgumentException("Cart is empty"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyOrders_Success() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(order));
        when(orderMapper.toOrderDTOList(anyList())).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    void getOrderById_Success_Owner() throws Exception {
        when(orderService.getOrderById(10L)).thenReturn(order);
        when(orderMapper.toOrderDTO(order)).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getOrderById_Forbidden_NotOwner() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(99L);
        order.setUser(anotherUser);

        when(orderService.getOrderById(10L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrderById_Success_Admin() throws Exception {
        // Change security context to admin, order belongs to normal user
        mockSecurityContext(adminUser);

        when(orderService.getOrderById(10L)).thenReturn(order);
        when(orderMapper.toOrderDTO(order)).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void getAllOrders_Success_Admin() throws Exception {
        mockSecurityContext(adminUser);

        when(orderService.getAllOrders()).thenReturn(List.of(order));
        when(orderMapper.toOrderDTOList(anyList())).thenReturn(List.of(orderDTO));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    void getAllOrders_Forbidden_NotAdmin() throws Exception {
        // Standard user context
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrder_Success_Admin() throws Exception {
        mockSecurityContext(adminUser);

        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setOrderNumber("ORD-12345");
        updateDTO.setStatus("SHIPPED");

        when(orderService.getOrderById(10L)).thenReturn(order);
        when(orderService.updateOrder(any(OrderDTO.class))).thenReturn(order);
        
        OrderDTO returnedDTO = new OrderDTO();
        returnedDTO.setId(10L);
        returnedDTO.setStatus("SHIPPED");
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(returnedDTO);

        mockMvc.perform(put("/api/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void updateOrder_Success_UserCancelPending() throws Exception {
        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setOrderNumber("ORD-12345");
        updateDTO.setStatus("CANCELLED");

        when(orderService.getOrderById(10L)).thenReturn(order); // Order status is PENDING
        when(orderService.updateOrder(any(OrderDTO.class))).thenReturn(order);
        
        OrderDTO returnedDTO = new OrderDTO();
        returnedDTO.setId(10L);
        returnedDTO.setStatus("CANCELLED");
        when(orderMapper.toOrderDTO(any(Order.class))).thenReturn(returnedDTO);

        mockMvc.perform(put("/api/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void updateOrder_Forbidden_UserCannotSetPaid() throws Exception {
        OrderDTO updateDTO = new OrderDTO();
        updateDTO.setOrderNumber("ORD-12345");
        updateDTO.setStatus("PAID"); // Standard user cannot pay/ship orders directly

        when(orderService.getOrderById(10L)).thenReturn(order);

        mockMvc.perform(put("/api/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }
}
