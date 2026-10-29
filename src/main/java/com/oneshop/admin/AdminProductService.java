package com.oneshop.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Product;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private static final int PAGE_SIZE = 10;

    private final ProductRepository products;
    private final ProductImageRepository images;
    private final OrderItemRepository orderItems;

    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, int page) {
        return products.findByNameContainingIgnoreCase(keyword == null ? "" : keyword.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional
    public void toggle(Long id) {
        Product product = get(id);
        product.setActive(!product.isActive());
    }

    @Transactional
    public void delete(Long id) {
        Product product = get(id);
        if (orderItems.existsByProductId(id)) {
            throw new BusinessException("Sản phẩm đã xuất hiện trong đơn hàng; hãy ẩn sản phẩm để giữ lịch sử đơn.");
        }
        images.deleteByProductId(id);
        products.delete(product);
        products.flush();
    }

    private Product get(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
    }
}
