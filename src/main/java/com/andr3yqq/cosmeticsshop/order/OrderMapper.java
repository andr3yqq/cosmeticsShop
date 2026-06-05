package com.andr3yqq.cosmeticsshop.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderDTO toOrderDTO(Order order);

    @Mapping(source = "userId", target = "user.id")
    Order toOrder(OrderDTO orderDTO);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "order.id", target = "orderId")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "orderId", target = "order.id")
    OrderItem toOrderItem(OrderItemDTO orderItemDTO);

    List<OrderDTO> toOrderDTOList(List<Order> orders);

    List<Order> toOrderList(List<OrderDTO> orderDTOs);
}
