package com.andr3yqq.cosmeticsshop.order;

import com.andr3yqq.cosmeticsshop.user.User;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order updateOrder(OrderDTO orderDTO);
    Order getOrderById(Long id);
    List<Order> getOrdersByUserId(Long id);
    Double getFullOrderPrice(OrderDTO orderDTO);
    List<Order> getAllOrders();

}
