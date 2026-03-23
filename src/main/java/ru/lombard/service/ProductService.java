package ru.lombard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.lombard.dto.ProductDto;
import ru.lombard.entity.Product;
import ru.lombard.entity.ProductImage;
import ru.lombard.entity.User;
import ru.lombard.repository.ProductRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.catalog-page-size:12}")
    private int pageSize;

    @Transactional(readOnly = true)
    public Page<ProductDto> findPublished(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                          Product.Condition condition, String search, int page) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("publishedAt").descending().and(Sort.by("id").descending()));
        String normalizedSearch = (search == null || search.isBlank())
                ? null
                : search.trim().toLowerCase(Locale.ROOT);
        Page<Product> products = productRepository.findPublishedWithFilters(
                Product.ProductStatus.PUBLISHED, categoryId, minPrice, maxPrice, condition, normalizedSearch, pageable);
        return products.map(this::toDto);
    }

    public Page<Product> findAllForAdmin(int page, int size) {
        return productRepository.findAllWithCategory(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Transactional
    public Product getBySlugAndIncrementViews(String slug) {
        Product p = productRepository.findBySlug(slug).orElse(null);
        if (p != null) {
            p.setViewsCount(p.getViewsCount() + 1);
            productRepository.save(p);
        }
        return p;
    }

    @Transactional
    public Product create(Product product, User createdBy, List<MultipartFile> imageFiles) throws IOException {
        product.setCreatedBy(createdBy);
        product.setCreatedAt(Instant.now());
        if (product.getSlug() == null || product.getSlug().isBlank()) {
            product.setSlug(generateSlug(product.getName()));
        }
        Product saved = productRepository.save(product);
        if (imageFiles != null && !imageFiles.isEmpty()) {
            addImages(saved, imageFiles);
        }
        return productRepository.save(saved);
    }

    @Transactional
    public Product update(Product product, List<MultipartFile> newImages) throws IOException {
        Product existing = productRepository.findById(product.getId()).orElseThrow();
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setCondition(product.getCondition());
        existing.setPrice(product.getPrice());
        existing.setQuantity(product.getQuantity());
        existing.setStatus(product.getStatus());
        existing.setYear(product.getYear());
        existing.setCategory(product.getCategory());
        if (newImages != null && !newImages.isEmpty()) {
            addImages(existing, newImages);
        }
        return productRepository.save(existing);
    }

    @Transactional
    public void publish(Long productId) {
        Product p = productRepository.findById(productId).orElseThrow();
        p.setStatus(Product.ProductStatus.PUBLISHED);
        p.setPublishedAt(Instant.now());
        productRepository.save(p);
    }

    @Transactional
    public void unpublish(Long productId) {
        Product p = productRepository.findById(productId).orElseThrow();
        p.setStatus(Product.ProductStatus.ARCHIVED);
        productRepository.save(p);
    }

    private void addImages(Product product, List<MultipartFile> files) throws IOException {
        Path dir = Paths.get(uploadDir, "products", product.getId().toString());
        Files.createDirectories(dir);
        int order = product.getImages().size();
        boolean first = product.getImages().isEmpty();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);
            String url = "/uploads/products/" + product.getId() + "/" + filename;
            ProductImage img = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .sortOrder(order++)
                    .main(first)
                    .build();
            product.getImages().add(img);
            first = false;
        }
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return null;
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private String generateSlug(String name) {
        String base = CategoryService.slugify(name);
        String slug = base;
        int n = 0;
        while (productRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + (++n);
        }
        return slug;
    }

    public ProductDto toDto(Product p) {
        List<String> urls = p.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        String mainUrl = p.getImages().stream().filter(ProductImage::isMain).map(ProductImage::getImageUrl).findFirst()
                .orElseGet(() -> p.getImages().isEmpty() ? null : p.getImages().get(0).getImageUrl());
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .condition(p.getCondition())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .status(p.getStatus())
                .year(p.getYear())
                .viewsCount(p.getViewsCount())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .createdAt(p.getCreatedAt())
                .publishedAt(p.getPublishedAt())
                .imageUrls(urls)
                .mainImageUrl(mainUrl)
                .build();
    }
}
