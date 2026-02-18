package com.andr3yqq.cosmeticsshop.discount;

public interface DiscountService {
    Discount createDiscount(DiscountDTO discountDTO);
    Discount updateDiscount(DiscountDTO discountDTO);
    Discount getDiscountById(Long id);
    Discount getDiscountForSpecificProductId(Long productId);
    Discount getDiscountForSpecificUserId(Long userId);
    Discount getDiscountForSpecificCategory(String category);
    Discount getDiscountForSpecificBrandId(Long brandId);
    boolean isDiscountActive(Discount discount);
}
