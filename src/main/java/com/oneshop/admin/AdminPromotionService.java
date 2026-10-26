package com.oneshop.admin;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.PromotionScope;
import com.oneshop.common.PromotionType;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Promotion;
import com.oneshop.repository.PromotionRepository;
import com.oneshop.vendor.PromotionForm;

import lombok.RequiredArgsConstructor;

/**
 * Admin/Manager quản lý khuyến mãi:
 *  - Tạo/sửa/xóa khuyến mãi TOÀN SÀN (giảm % sản phẩm, giảm phí vận chuyển).
 *  - Xem khuyến mãi của các shop và bật/tắt khi cần (không sửa nội dung của shop).
 * Việc tính giảm giá ở checkout do PromotionService (Jung) đảm nhiệm, mã toàn sàn dùng được cho mọi shop.
 */
@Service
@RequiredArgsConstructor
public class AdminPromotionService {

    private static final int PAGE_SIZE = 10;

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public Page<Promotion> list(PromotionScope scope, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return scope == null ? promotionRepository.findAllBy(pageable) : promotionRepository.findByScope(scope, pageable);
    }

    /** Lấy khuyến mãi TOÀN SÀN để sửa; khuyến mãi của shop thì không được sửa ở đây. */
    @Transactional(readOnly = true)
    public Promotion getPlatform(Long id) {
        Promotion promotion = find(id);
        if (promotion.getScope() != PromotionScope.PLATFORM) {
            throw new BusinessException("Chỉ sửa được khuyến mãi toàn sàn. Khuyến mãi của shop do Vendor quản lý.");
        }
        return promotion;
    }

    @Transactional
    public Promotion create(PromotionForm f) {
        String code = f.getCode().trim().toUpperCase();
        if (promotionRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Mã này đã tồn tại, hãy chọn mã khác.");
        }
        validate(f);
        Promotion promotion = Promotion.builder()
                .code(code)
                .scope(PromotionScope.PLATFORM)
                .shop(null)
                .build();
        apply(promotion, f);
        return promotionRepository.save(promotion);
    }

    /** Không cho đổi mã sau khi tạo (mã có thể đã được khách lưu). */
    @Transactional
    public Promotion update(Long id, PromotionForm f) {
        Promotion promotion = getPlatform(id);
        validate(f);
        if (f.getQuantity() < promotion.getUsed()) {
            throw new BusinessException("Số lượng không được nhỏ hơn số lượt đã dùng (" + promotion.getUsed() + ").");
        }
        apply(promotion, f);
        return promotion;
    }

    /** Bật/tắt khuyến mãi bất kỳ (kể cả của shop, ví dụ khi phát hiện vi phạm). */
    @Transactional
    public void toggleActive(Long id) {
        Promotion promotion = find(id);
        promotion.setActive(!promotion.isActive());
    }

    @Transactional
    public void delete(Long id) {
        Promotion promotion = getPlatform(id);
        if (promotion.getUsed() > 0) {
            throw new BusinessException("Mã đã có người sử dụng nên không thể xóa. Hãy tắt mã thay vì xóa.");
        }
        promotionRepository.delete(promotion);
        try {
            promotionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Mã đang được dùng trong dữ liệu khác nên không thể xóa. Hãy tắt mã.");
        }
    }

    private Promotion find(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi."));
    }

    private void validate(PromotionForm f) {
        if (f.getStartAt() == null || f.getEndAt() == null || f.getType() == null || f.getValue() == null
                || f.getQuantity() == null || f.getMinOrder() == null) {
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin khuyến mãi.");
        }
        if (f.getType() != PromotionType.PERCENT_PRODUCT && f.getType() != PromotionType.SHIPPING_DISCOUNT) {
            throw new BusinessException("Khuyến mãi toàn sàn chỉ hỗ trợ giảm % sản phẩm hoặc giảm phí vận chuyển.");
        }
        if (!f.getEndAt().isAfter(f.getStartAt())) {
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        if (f.getType() == PromotionType.PERCENT_PRODUCT
                && f.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Giảm theo % tối đa là 100.");
        }
    }

    private void apply(Promotion promotion, PromotionForm f) {
        promotion.setName(f.getName().trim());
        promotion.setType(f.getType());
        promotion.setValue(f.getValue());
        promotion.setMinOrder(f.getMinOrder());
        promotion.setMaxDiscount(f.getType() == PromotionType.PERCENT_PRODUCT ? f.getMaxDiscount() : null);
        promotion.setStartAt(f.getStartAt());
        promotion.setEndAt(f.getEndAt());
        promotion.setQuantity(f.getQuantity());
        promotion.setActive(f.isActive());
    }
}