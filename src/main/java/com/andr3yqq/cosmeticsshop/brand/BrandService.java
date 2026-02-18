package com.andr3yqq.cosmeticsshop.brand;

public interface BrandService {
    Brand createBrand(BrandDTO brandDTO);
    Brand updateBrand(BrandDTO brandDTO);
    Brand getBrandById(Long id);
}
