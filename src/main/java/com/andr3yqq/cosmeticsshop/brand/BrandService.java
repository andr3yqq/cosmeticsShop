package com.andr3yqq.cosmeticsshop.brand;

import java.util.List;

public interface BrandService {
    Brand createBrand(BrandDTO brandDTO);
    Brand updateBrand(BrandDTO brandDTO);
    Brand getBrandById(Long id);
    List<Brand> getAllBrands();
    void deleteBrand(Long id);
}
