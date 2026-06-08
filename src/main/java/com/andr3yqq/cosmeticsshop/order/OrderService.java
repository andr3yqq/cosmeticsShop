package com.andr3yqq.cosmeticsshop.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order updateOrder(OrderDTO orderDTO);
    Order getOrderById(Long id);
    Page<Order> getOrdersByUserId(Long id, Pageable pageable);
    Double getFullOrderPrice(OrderDTO orderDTO);
    Page<Order> getAllOrders(Pageable pageable);

}
