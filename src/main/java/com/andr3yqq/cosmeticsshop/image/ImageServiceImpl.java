package com.andr3yqq.cosmeticsshop.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @Override
    public Image createImage(ImageDTO imageDTO) {
        Image image = imageMapper.toImage(imageDTO);
        return imageRepository.save(image);
    }

    @Override
    public Image updateImage(ImageDTO imageDTO) {
        if (imageDTO.getId() == null) {
            throw new IllegalArgumentException("Image ID cannot be null for updates");
        }
        Image existingImage = imageRepository.findById(imageDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + imageDTO.getId()));

        existingImage.setImageUrl(imageDTO.getImageUrl());
        existingImage.setName(imageDTO.getName());
        existingImage.setType(imageDTO.getType());

        return imageRepository.save(existingImage);
    }

    @Override
    @Transactional(readOnly = true)
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Image> getAllImages(Pageable pageable) {
        return imageRepository.findAll(pageable);
    }

    @Override
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + id));
        imageRepository.delete(image);
    }
}
