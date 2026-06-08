package com.andr3yqq.cosmeticsshop.image;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper imageMapper;

    @GetMapping
    public ResponseEntity<Page<ImageDTO>> getAllImages(Pageable pageable) {
        Page<ImageDTO> images = imageService.getAllImages(pageable)
                .map(imageMapper::toImageDTO);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageDTO> getImageById(@PathVariable Long id) {
        Image image = imageService.getImageById(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(imageMapper.toImageDTO(image));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageDTO> createImage(@Valid @RequestBody ImageDTO imageDTO) {
        Image savedImage = imageService.createImage(imageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageMapper.toImageDTO(savedImage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageDTO> updateImage(@PathVariable Long id, @Valid @RequestBody ImageDTO imageDTO) {
        imageDTO.setId(id);
        Image updatedImage = imageService.updateImage(imageDTO);
        return ResponseEntity.ok(imageMapper.toImageDTO(updatedImage));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
