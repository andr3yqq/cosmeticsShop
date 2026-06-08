package com.andr3yqq.cosmeticsshop.image;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ImageService {
    Image createImage(ImageDTO imageDTO);
    Image updateImage(ImageDTO imageDTO);
    Image getImageById(Long id);
    Page<Image> getAllImages(Pageable pageable);
    void deleteImage(Long id);
}
