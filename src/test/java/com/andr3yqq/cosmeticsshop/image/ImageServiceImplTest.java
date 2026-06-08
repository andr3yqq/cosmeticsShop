package com.andr3yqq.cosmeticsshop.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @InjectMocks
    private ImageServiceImpl imageService;

    private ImageDTO imageDTO;
    private Image image;

    @BeforeEach
    void setUp() {
        imageDTO = new ImageDTO(1L, "http://example.com/image.jpg", "product-image", "jpg");
        image = new Image(1L, "http://example.com/image.jpg", "product-image", "jpg");
    }

    @Test
    void createImage_Success() {
        when(imageMapper.toImage(imageDTO)).thenReturn(image);
        when(imageRepository.save(image)).thenReturn(image);

        Image result = imageService.createImage(imageDTO);

        assertNotNull(result);
        assertEquals("http://example.com/image.jpg", result.getImageUrl());
        verify(imageRepository, times(1)).save(image);
    }

    @Test
    void updateImage_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Image result = imageService.updateImage(imageDTO);

        assertNotNull(result);
        assertEquals("product-image", result.getName());
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    void updateImage_NullId_ThrowsException() {
        imageDTO.setId(null);

        assertThrows(IllegalArgumentException.class, () -> imageService.updateImage(imageDTO));
        verify(imageRepository, never()).save(any());
    }

    @Test
    void updateImage_NotFound_ThrowsException() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> imageService.updateImage(imageDTO));
        verify(imageRepository, never()).save(any());
    }

    @Test
    void getImageById_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        Image result = imageService.getImageById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getImageById_NotFound_ReturnsNull() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        Image result = imageService.getImageById(1L);

        assertNull(result);
    }

    @Test
    void getAllImages_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(imageRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(image)));

        Page<Image> result = imageService.getAllImages(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("product-image", result.getContent().getFirst().getName());
    }

    @Test
    void deleteImage_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        doNothing().when(imageRepository).delete(image);

        imageService.deleteImage(1L);

        verify(imageRepository, times(1)).delete(image);
    }
}
