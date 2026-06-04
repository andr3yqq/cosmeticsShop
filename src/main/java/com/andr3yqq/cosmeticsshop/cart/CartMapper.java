package com.andr3yqq.cosmeticsshop.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    CartDTO toCartDTO(Cart cart);

    @Mapping(source = "userId", target = "user.id")
    Cart toCart(CartDTO cartDTO);

    @Mapping(source = "product.id", target = "productId")
    CartItemDTO toCartItemDTO(CartItem cartItem);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(target = "cart", ignore = true)
    CartItem toCartItem(CartItemDTO cartItemDTO);

    List<CartDTO> toCartDTOList(List<Cart> carts);

    List<Cart> toCartList(List<CartDTO> cartDTOList);
}
