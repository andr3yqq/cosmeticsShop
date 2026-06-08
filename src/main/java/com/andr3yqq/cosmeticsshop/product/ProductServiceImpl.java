package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.brand.BrandRepository;
import com.andr3yqq.cosmeticsshop.image.Image;
import com.andr3yqq.cosmeticsshop.image.ImageDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        if (brandRepository.findByName(productDTO.getBrand()).isEmpty()) {
            throw new IllegalArgumentException("Brand not found with name: " + productDTO.getBrand());
        }
        if (getProductBySku(productDTO.getSku()) == null) {
            Product product = new Product(
                    productDTO.getSku(),
                    productDTO.getBrand(),
                    productDTO.getName(),
                    productDTO.getDescription(),
                    productDTO.getPrice(),
                    productDTO.getAvailableStock(),
                    ProductStatus.valueOf(productDTO.getStatus()),
                    ProductCategory.valueOf(productDTO.getCategory())
            );
            
            product.setFeatures(productDTO.getFeatures());
            
            if (productDTO.getImages() != null) {
                List<Image> images = new ArrayList<>();
                for (ImageDTO imageDTO : productDTO.getImages()) {
                    Image image = new Image(imageDTO.getId(), imageDTO.getImageUrl(), imageDTO.getName(), imageDTO.getType());
                    image.setProduct(product);
                    images.add(image);
                }
                product.setImages(images);
            }
            
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    @Transactional
    public Product updateProduct(ProductDTO productDTO) {
        if (brandRepository.findByName(productDTO.getBrand()).isEmpty()) {
            throw new IllegalArgumentException("Brand not found with name: " + productDTO.getBrand());
        }
        Product product = getProductById(productDTO.getId());
        if (product != null) {
            product.setSku(productDTO.getSku());
            product.setBrand(productDTO.getBrand());
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setLastPrice(product.getPrice());
            product.setPrice(productDTO.getPrice());
            product.setAvailableStock(productDTO.getAvailableStock());
            product.setCategory(ProductCategory.valueOf(productDTO.getCategory()));
            product.setStatus(ProductStatus.valueOf(productDTO.getStatus()));
            
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            } else {
                product.getImages().clear();
            }

            if (productDTO.getImages() != null) {
                for (ImageDTO imageDTO : productDTO.getImages()) {
                    Image image = new Image(imageDTO.getId(), imageDTO.getImageUrl(), imageDTO.getName(), imageDTO.getType());
                    image.setProduct(product);
                    product.getImages().add(image);
                }
            }
            
            product.setFeatures(productDTO.getFeatures());
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public Page<Product> getProductsByBrand(String brand, Pageable pageable) {
        return productRepository.findAllByBrand(brand, pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        ProductCategory productCategory = ProductCategory.valueOf(category);
        return productRepository.findAllByCategory(productCategory, pageable);
    }

    @Override
    public Page<Product> getProductsByName(String name, Pageable pageable) {
        return productRepository.findAllByName(name, pageable);
    }
}
