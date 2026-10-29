package com.oneshop.admin;

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
import com.oneshop.vendor.PromotionForm;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPlatformPromotionService {
    private static final int PAGE_SIZE = 10;
    private final PromotionRepository promotions;

    @Transactional(readOnly = true)
    public Page<Promotion> list(int page) {
        return promotions.findByScope(PromotionScope.PLATFORM,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public Promotion get(Long id) {
        return promotions.findByIdAndScope(id, PromotionScope.PLATFORM)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi toàn sàn."));
    }

    @Transactional
    public Promotion create(PromotionForm form) {
        String code = normalizeCode(form.getCode());
        if (promotions.existsByCodeIgnoreCase(code)) throw new BusinessException("Mã khuyến mãi đã tồn tại.");
        validate(form);
        Promotion promotion = Promotion.builder().code(code).scope(PromotionScope.PLATFORM).shop(null).build();
        apply(promotion, form);
        return promotions.save(promotion);
    }

    @Transactional
    public Promotion update(Long id, PromotionForm form) {
        Promotion promotion = get(id);
        validate(form);
        if (form.getQuantity() < promotion.getUsed())
            throw new BusinessException("Số lượng không được thấp hơn số mã đã dùng.");
        String code = normalizeCode(form.getCode());
        if (!promotion.getCode().equalsIgnoreCase(code) && promotions.existsByCodeIgnoreCase(code))
            throw new BusinessException("Mã khuyến mãi đã tồn tại.");
        promotion.setCode(code);
        apply(promotion, form);
        return promotion;
    }

    @Transactional
    public void toggle(Long id) {
        Promotion promotion = get(id);
        promotion.setActive(!promotion.isActive());
    }

    @Transactional
    public void delete(Long id) {
        Promotion promotion = get(id);
        if (promotion.getUsed() > 0)
            throw new BusinessException("Mã đã được sử dụng; hãy tắt mã thay vì xóa để giữ lịch sử.");
        promotions.delete(promotion);
        promotions.flush();
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private void validate(PromotionForm form) {
        if (form.getStartAt() == null || form.getEndAt() == null || form.getType() == null
                || form.getValue() == null || form.getQuantity() == null || form.getMinOrder() == null)
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin khuyến mãi.");
        if (!form.getEndAt().isAfter(form.getStartAt()))
            throw new BusinessException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        if (form.getType() == PromotionType.PERCENT_PRODUCT
                && form.getValue().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new BusinessException("Mức giảm phần trăm tối đa là 100%.");
    }

    private void apply(Promotion promotion, PromotionForm form) {
        promotion.setName(form.getName().trim());
        promotion.setType(form.getType());
        promotion.setValue(form.getValue());
        promotion.setMinOrder(form.getMinOrder());
        promotion.setMaxDiscount(form.getType() == PromotionType.PERCENT_PRODUCT ? form.getMaxDiscount() : null);
        promotion.setStartAt(form.getStartAt());
        promotion.setEndAt(form.getEndAt());
        promotion.setQuantity(form.getQuantity());
        promotion.setActive(form.isActive());
    }
}
