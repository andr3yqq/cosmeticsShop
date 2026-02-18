package com.andr3yqq.cosmeticsshop.cart;

public interface CartService {
    Cart createCart(CartDTO cartDTO);
    Cart updateCart(CartDTO cartDTO);
    Cart clearCart(CartDTO cartDTO);
    Cart getCardById(Long id);
    CartItem getCartItemById(Long id);
    CartItem updateCartItem(CartItemDTO cartItemDTO);
    CartItem deleteCartItemById(Long id);
    Double getFullCartPrice(CartDTO cartDTO);
}
