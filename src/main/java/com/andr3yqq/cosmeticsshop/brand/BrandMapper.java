package com.andr3yqq.cosmeticsshop.brand;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    @Mapping(source = "logo.id", target = "imageId")
    BrandDTO toBrandDTO(Brand brand);

    @Mapping(source = "imageId", target = "logo.id")
    Brand toBrand(BrandDTO brandDTO);

    List<BrandDTO> toBrandDTOList(List<Brand> brands);

    List<Brand> toBrandList(List<BrandDTO> brandDTOs);
}
