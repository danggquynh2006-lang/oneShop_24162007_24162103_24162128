package com.oneshop.admin;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Product;
import com.oneshop.entity.Shop;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

/**
 * Admin/Manager quản lý sản phẩm của mọi shop: tìm kiếm, khóa/mở khóa, xóa.
 * Quy ước: sản phẩm bị khóa luôn có active = false, nên các trang bán hàng chỉ cần lọc active = true.
 */
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private static final int PAGE_SIZE = 10;

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;

    /** Tìm theo từ khóa (tên, thương hiệu), shop, trạng thái (active / hidden / locked); có phân trang. */
    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, Long shopId, String status, int page) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("name")), like),
                    cb.like(cb.lower(root.<String>get("brand")), like)));
        }
        if (shopId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("shop").<Long>get("id"), shopId));
        }
        if ("active".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.<Boolean>get("active")));
        } else if ("hidden".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.and(
                    cb.isFalse(root.<Boolean>get("active")),
                    cb.or(cb.isNull(root.<Boolean>get("locked")), cb.isFalse(root.<Boolean>get("locked")))));
        } else if ("locked".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.<Boolean>get("locked")));
        }

        return productRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public List<Shop> shops() {
        return shopRepository.findAll(Sort.by("name"));
    }

    /** Khóa: ẩn khỏi cửa hàng và Vendor không bật lại được. */
    @Transactional
    public void lock(Long id) {
        Product product = get(id);
        product.setLocked(true);
        product.setActive(false);
    }

    /** Mở khóa: sản phẩm hiển thị trở lại. */
    @Transactional
    public void unlock(Long id) {
        Product product = get(id);
        product.setLocked(false);
        product.setActive(true);
    }

    /** Chỉ xóa được sản phẩm chưa từng phát sinh đơn hàng; ngược lại phải khóa. */
    @Transactional
    public void delete(Long id) {
        Product product = get(id);
        if (product.getSold() > 0 || orderItemRepository.existsByProductId(id)) {
            throw new BusinessException("Sản phẩm đã phát sinh đơn hàng, không thể xóa. Hãy khóa sản phẩm thay vì xóa.");
        }
        imageRepository.deleteByProductId(id);
        productRepository.delete(product);
        try {
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Sản phẩm đang được dùng ở dữ liệu khác nên không thể xóa. Hãy khóa sản phẩm thay vì xóa.");
        }
    }

    private Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
    }
}