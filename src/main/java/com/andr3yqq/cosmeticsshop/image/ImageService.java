package com.andr3yqq.cosmeticsshop.image;

public interface ImageService {
    Image createImage(ImageDTO imageDTO);
    Image updateImage(ImageDTO imageDTO);
    Image getImageById(Long id);
}
