package com.andr3yqq.cosmeticsshop.product;

import com.andr3yqq.cosmeticsshop.image.Image;
import com.andr3yqq.cosmeticsshop.image.ImageDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        if (getProductBySku(productDTO.getSku()) != null) {
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
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    @Transactional
    public Product updateProduct(ProductDTO productDTO) {
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
            List<Image> images = new ArrayList<>();
            for (ImageDTO imageDTO : productDTO.getImages())
            {
                Image image = new Image(imageDTO.getId(), imageDTO.getImageUrl(), imageDTO.getName(), imageDTO.getType());
                images.add(image);
            }
            product.setImages(images);
            product.setFeatures(productDTO.getFeatures());
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
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
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findAllByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        ProductCategory productCategory = ProductCategory.valueOf(category);
        return productRepository.findAllByCategory(productCategory);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findAllByName(name);
    }
}
