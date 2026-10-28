package com.oneshop.vendor;

import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.config.CloudinaryService;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductImage;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class VendorProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<Product> list(Long shopId, String query, int page) {
        return productRepository.findByShopIdAndNameContainingIgnoreCase(shopId,
                query == null ? "" : query.trim(), PageRequest.of(Math.max(0, page), 10, Sort.by("id").descending()));
    }

    @Transactional(readOnly = true)
    public Product get(Long shopId, Long id) {
        return productRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm của shop."));
    }

    @Transactional(readOnly = true)
    public List<ProductImage> images(Long productId) {
        return imageRepository.findByProductIdOrderByMainImageDescIdAsc(productId);
    }

    @Transactional
    public Product create(Long shopId, ProductForm form, MultipartFile[] files) throws IOException {
        Product product = new Product();
        product.setShop(shopRepository.getReferenceById(shopId));
        apply(product, form);
        productRepository.save(product);
        uploadImages(product, files);
        return product;
    }

    @Transactional
    public void update(Long shopId, Long id, ProductForm form, MultipartFile[] files) throws IOException {
        Product product = get(shopId, id);
        apply(product, form);
        uploadImages(product, files);
    }

    @Transactional
    public void toggle(Long shopId, Long id) {
        Product product = get(shopId, id);
        product.setActive(!product.isActive());
    }

    @Transactional
    public void delete(Long shopId, Long id) {
        Product product = get(shopId, id);
        if (product.getSold() > 0) throw new BusinessException("Sản phẩm đã bán, hãy ẩn thay vì xóa.");
        imageRepository.deleteByProductId(id);
        productRepository.delete(product);
    }

    @Transactional
    public void removeImage(Long shopId, Long productId, Long imageId) {
        get(shopId, productId);
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh."));
        if (!image.getProduct().getId().equals(productId)) throw new ResourceNotFoundException("Không tìm thấy ảnh.");
        imageRepository.delete(image);
    }

    private void apply(Product product, ProductForm form) {
        var category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new BusinessException("Danh mục không tồn tại."));
        if (!category.isActive()) throw new BusinessException("Danh mục đã ngừng sử dụng.");
        product.setName(form.getName().trim());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setDiscountPercent(form.getDiscountPercent());
        product.setStock(form.getStock());
        product.setCategory(category);
        product.setBrand(form.getBrand());
        product.setActive(form.isActive());
    }

    private void uploadImages(Product product, MultipartFile[] files) throws IOException {
        if (files == null) return;
        boolean main = imageRepository.findByProductIdOrderByMainImageDescIdAsc(product.getId()).isEmpty();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            if (file.getSize() > 10 * 1024 * 1024 || file.getContentType() == null
                    || !file.getContentType().startsWith("image/")) {
                throw new BusinessException("Chỉ nhận ảnh tối đa 10 MB.");
            }
            var uploaded = cloudinaryService.uploadImage(file, "products");
            imageRepository.save(ProductImage.builder().product(product).url(uploaded.url())
                    .publicId(uploaded.publicId()).mainImage(main).build());
            main = false;
        }
    }
}
