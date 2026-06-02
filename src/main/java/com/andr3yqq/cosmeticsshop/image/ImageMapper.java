package com.andr3yqq.cosmeticsshop.image;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageDTO toImageDTO(Image image);

    Image toImage(ImageDTO imageDTO);

    List<ImageDTO> toImageDTOList(List<Image> images);

    List<Image> toImageList(List<ImageDTO> imageDTOs);
}
