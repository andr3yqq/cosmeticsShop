package com.andr3yqq.cosmeticsshop.brand;

import com.andr3yqq.cosmeticsshop.image.Image;
import com.andr3yqq.cosmeticsshop.image.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ImageRepository imageRepository;
    private final BrandMapper brandMapper;

    @Override
    public Brand createBrand(BrandDTO brandDTO) {
        if (brandRepository.findByName(brandDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Brand with name " + brandDTO.getName() + " already exists");
        }
        Brand brand = brandMapper.toBrand(brandDTO);
        if (brandDTO.getImageId() != null) {
            Image logo = imageRepository.findById(brandDTO.getImageId())
                    .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + brandDTO.getImageId()));
            brand.setLogo(logo);
        } else {
            brand.setLogo(null);
        }
        return brandRepository.save(brand);
    }

    @Override
    public Brand updateBrand(BrandDTO brandDTO) {
        if (brandDTO.getId() == null) {
            throw new IllegalArgumentException("Brand ID cannot be null for updates");
        }
        Brand existingBrand = brandRepository.findById(brandDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandDTO.getId()));

        if (!existingBrand.getName().equals(brandDTO.getName()) &&
                brandRepository.findByName(brandDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Brand with name " + brandDTO.getName() + " already exists");
        }

        existingBrand.setName(brandDTO.getName());
        existingBrand.setDescription(brandDTO.getDescription());

        if (brandDTO.getImageId() != null) {
            Image logo = imageRepository.findById(brandDTO.getImageId())
                    .orElseThrow(() -> new IllegalArgumentException("Image not found with id: " + brandDTO.getImageId()));
            existingBrand.setLogo(logo);
        } else {
            existingBrand.setLogo(null);
        }

        return brandRepository.save(existingBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public Brand getBrandById(Long id) {
        return brandRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + id));
        brandRepository.delete(brand);
    }
}
