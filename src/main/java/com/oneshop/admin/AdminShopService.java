package com.oneshop.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Shop;
import com.oneshop.repository.ShopRepository;
import com.oneshop.vendor.ShopService;

import lombok.RequiredArgsConstructor;

/**
 * Admin/Manager quản lý shop: duyệt, từ chối, khóa/mở khóa, cấu hình % chiết khấu sàn.
 * Việc duyệt dùng lại ShopService.approve() của Vendor để chỉ có một nơi đổi chủ shop thành VENDOR.
 */
@Service
@RequiredArgsConstructor
public class AdminShopService {

    private static final int PAGE_SIZE = 10;
    private static final BigDecimal MAX_RATE = new BigDecimal("100");

    private final ShopRepository shopRepository;
    private final ShopService shopService;

    @Transactional(readOnly = true)
    public Page<Shop> search(String keyword, ShopStatus status, int page) {
        String kw = keyword == null ? "" : keyword.trim();
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return status == null
                ? shopRepository.searchAll(kw, pageable)
                : shopRepository.searchByStatus(status, kw, pageable);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return shopRepository.countByStatus(ShopStatus.PENDING);
    }

    /** Duyệt shop: PENDING -> ACTIVE và chủ shop thành VENDOR. */
    public void approve(Long id) {
        shopService.approve(id);
    }

    /** Từ chối đăng ký: chỉ xóa được shop đang chờ duyệt (chưa có sản phẩm/đơn nào). */
    @Transactional
    public void reject(Long id) {
        Shop shop = get(id);
        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new BusinessException("Chỉ từ chối được shop đang chờ duyệt.");
        }
        shopRepository.delete(shop);
    }

    @Transactional
    public void lock(Long id) {
        Shop shop = get(id);
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new BusinessException("Chỉ khóa được shop đang hoạt động.");
        }
        shop.setStatus(ShopStatus.LOCKED);
    }

    @Transactional
    public void unlock(Long id) {
        Shop shop = get(id);
        if (shop.getStatus() != ShopStatus.LOCKED) {
            throw new BusinessException("Shop này không bị khóa.");
        }
        shop.setStatus(ShopStatus.ACTIVE);
    }

    /** Cấu hình % chiết khấu sàn của shop (0 - 100, tối đa 2 chữ số thập phân). */
    @Transactional
    public void updateCommission(Long id, BigDecimal rate) {
        if (rate == null) {
            throw new BusinessException("Vui lòng nhập % chiết khấu.");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(MAX_RATE) > 0) {
            throw new BusinessException("% chiết khấu phải từ 0 đến 100.");
        }
        get(id).setCommissionRate(rate.setScale(2, RoundingMode.HALF_UP));
    }

    private Shop get(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shop."));
    }
}