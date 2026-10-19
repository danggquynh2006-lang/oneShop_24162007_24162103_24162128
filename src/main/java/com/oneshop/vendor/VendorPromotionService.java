package com.oneshop.vendor;

import java.math.BigDecimal;

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
import com.oneshop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorPromotionService {

    private static final int PAGE_SIZE = 10;

    private final PromotionRepository promotionRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public Page<Promotion> list(Long shopId, int page) {
        return promotionRepository.findByShopId(shopId,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    /** Chỉ lấy khuyến mãi CỦA SHOP NÀY, của shop khác trả về 404. */
    @Transactional(readOnly = true)
    public Promotion get(Long shopId, Long id) {
        return promotionRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi."));
    }

    @Transactional
    public Promotion create(Long shopId, PromotionForm f) {
        String code = f.getCode().trim().toUpperCase();
        if (promotionRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Mã này đã tồn tại, hãy chọn mã khác.");
        }
        validate(f);
        Promotion p = Promotion.builder()
                .code(code)
                .scope(PromotionScope.SHOP)
                .shop(shopRepository.getReferenceById(shopId))
                .build();
        apply(p, f);
        return promotionRepository.save(p);
    }

    /** Không cho đổi mã sau khi tạo (mã có thể đã được khách lưu). */
    @Transactional
    public Promotion update(Long shopId, Long id, PromotionForm f) {
        Promotion p = get(shopId, id);
        validate(f);
        if (f.getQuantity() < p.getUsed()) {
            throw new BusinessException("Số lượng không được nhỏ hơn số lượt đã dùng (" + p.getUsed() + ").");
        }
        apply(p, f);
        return p;
    }

    @Transactional
    public void toggleActive(Long shopId, Long id) {
        Promotion p = get(shopId, id);
        p.setActive(!p.isActive());
    }

    @Transactional
    public void delete(Long shopId, Long id) {
        Promotion p = get(shopId, id);
        if (p.getUsed() > 0) {
            throw new BusinessException("Mã đã có người sử dụng nên không thể xóa. Hãy tắt mã thay vì xóa.");
        }
        promotionRepository.delete(p);
        promotionRepository.flush();
    }

    private void validate(PromotionForm f) {
        if (f.getStartAt() == null || f.getEndAt() == null || f.getType() == null || f.getValue() == null
                || f.getQuantity() == null || f.getMinOrder() == null) {
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin khuyến mãi.");
        }
        if (!f.getEndAt().isAfter(f.getStartAt())) {
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        if (f.getType() == PromotionType.PERCENT_PRODUCT
                && f.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Giảm theo % tối đa là 100.");
        }
    }

    private void apply(Promotion p, PromotionForm f) {
        p.setName(f.getName().trim());
        p.setType(f.getType());
        p.setValue(f.getValue());
        p.setMinOrder(f.getMinOrder());
        p.setMaxDiscount(f.getType() == PromotionType.PERCENT_PRODUCT ? f.getMaxDiscount() : null);
        p.setStartAt(f.getStartAt());
        p.setEndAt(f.getEndAt());
        p.setQuantity(f.getQuantity());
        p.setActive(f.isActive());
    }
}
