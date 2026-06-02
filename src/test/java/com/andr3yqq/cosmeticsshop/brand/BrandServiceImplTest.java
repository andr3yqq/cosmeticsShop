package com.andr3yqq.cosmeticsshop.brand;

import com.andr3yqq.cosmeticsshop.image.Image;
import com.andr3yqq.cosmeticsshop.image.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandServiceImpl brandService;

    private BrandDTO brandDTO;
    private Brand brand;
    private Image logo;

    @BeforeEach
    void setUp() {
        logo = new Image(1L, "http://example.com/logo.jpg", "logo", "jpg");
        brandDTO = new BrandDTO(1L, "Loreal", "Loreal description", 1L);
        brand = new Brand(1L, "Loreal", "Loreal description", logo);
    }

    @Test
    void createBrand_Success() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.empty());
        when(brandMapper.toBrand(brandDTO)).thenReturn(brand);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(logo));
        when(brandRepository.save(brand)).thenReturn(brand);

        Brand result = brandService.createBrand(brandDTO);

        assertNotNull(result);
        assertEquals("Loreal", result.getName());
        assertEquals(logo, result.getLogo());
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void createBrand_DuplicateName_ThrowsException() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.of(brand));

        assertThrows(IllegalArgumentException.class, () -> brandService.createBrand(brandDTO));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void createBrand_ImageNotFound_ThrowsException() {
        when(brandRepository.findByName("Loreal")).thenReturn(Optional.empty());
        when(brandMapper.toBrand(brandDTO)).thenReturn(brand);
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> brandService.createBrand(brandDTO));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrand_Success() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(imageRepository.findById(1L)).thenReturn(Optional.of(logo));
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Brand result = brandService.updateBrand(brandDTO);

        assertNotNull(result);
        assertEquals("Loreal", result.getName());
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    @Test
    void updateBrand_NullId_ThrowsException() {
        brandDTO.setId(null);

        assertThrows(IllegalArgumentException.class, () -> brandService.updateBrand(brandDTO));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrand_NotFound_ThrowsException() {
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> brandService.updateBrand(brandDTO));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void getBrandById_Success() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        Brand result = brandService.getBrandById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getBrandById_NotFound_ReturnsNull() {
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        Brand result = brandService.getBrandById(1L);

        assertNull(result);
    }

    @Test
    void getAllBrands_Success() {
        when(brandRepository.findAll()).thenReturn(List.of(brand));

        List<Brand> result = brandService.getAllBrands();

        assertEquals(1, result.size());
        assertEquals("Loreal", result.getFirst().getName());
    }

    @Test
    void deleteBrand_Success() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        doNothing().when(brandRepository).delete(brand);

        brandService.deleteBrand(1L);

        verify(brandRepository, times(1)).delete(brand);
    }
}
