package com.andr3yqq.cosmeticsshop.image;

import java.util.List;

public interface ImageService {
    Image createImage(ImageDTO imageDTO);
    Image updateImage(ImageDTO imageDTO);
    Image getImageById(Long id);
    List<Image> getAllImages();
    void deleteImage(Long id);
}
