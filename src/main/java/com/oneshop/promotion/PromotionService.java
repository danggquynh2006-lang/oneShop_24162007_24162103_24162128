package com.oneshop.promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.PromotionScope;
import com.oneshop.common.PromotionType;
import com.oneshop.entity.Promotion;
import com.oneshop.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public record Discount(BigDecimal productDiscount, BigDecimal shippingDiscount, BigDecimal total) {}

    @Transactional(readOnly = true)
    public Discount calculate(String code, Long shopId, BigDecimal subtotal, BigDecimal shippingFee) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại."));
        validate(promotion, shopId, subtotal);
        return discount(promotion, subtotal, shippingFee);
    }

    @Transactional
    public Discount redeem(String code, Long shopId, BigDecimal subtotal, BigDecimal shippingFee) {
        Promotion promotion = promotionRepository.findWithLockByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại."));
        validate(promotion, shopId, subtotal);
        Discount result = discount(promotion, subtotal, shippingFee);
        promotion.setUsed(promotion.getUsed() + 1);
        return result;
    }

    private void validate(Promotion promotion, Long shopId, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive() || now.isBefore(promotion.getStartAt()) || now.isAfter(promotion.getEndAt()))
            throw new BusinessException("Mã giảm giá chưa có hiệu lực hoặc đã hết hạn.");
        if (promotion.getUsed() >= promotion.getQuantity()) throw new BusinessException("Mã giảm giá đã hết lượt.");
        if (promotion.getScope() == PromotionScope.SHOP &&
                (shopId == null || !promotion.getShop().getId().equals(shopId)))
            throw new BusinessException("Mã giảm giá không áp dụng cho shop này.");
        if (subtotal == null || subtotal.signum() < 0 || subtotal.compareTo(promotion.getMinOrder()) < 0)
            throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu.");
    }

    private Discount discount(Promotion promotion, BigDecimal subtotal, BigDecimal shippingFee) {
        BigDecimal safeShipping = shippingFee == null ? BigDecimal.ZERO : shippingFee.max(BigDecimal.ZERO);
        BigDecimal product = BigDecimal.ZERO;
        BigDecimal shipping = BigDecimal.ZERO;
        if (promotion.getType() == PromotionType.PERCENT_PRODUCT) {
            product = subtotal.multiply(promotion.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (promotion.getMaxDiscount() != null) product = product.min(promotion.getMaxDiscount());
            product = product.min(subtotal);
        } else if (promotion.getType() == PromotionType.FIXED_AMOUNT) {
            product = promotion.getValue().min(subtotal);
        } else if (promotion.getType() == PromotionType.SHIPPING_DISCOUNT) {
            shipping = promotion.getValue().min(safeShipping);
        }
        return new Discount(product, shipping, product.add(shipping));
    }
}
