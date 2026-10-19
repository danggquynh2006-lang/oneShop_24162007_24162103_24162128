package com.oneshop.order;

import java.math.BigDecimal;

public final class PriceCalculator {

    private PriceCalculator() {}

    public static BigDecimal total(BigDecimal subtotal,
                                   BigDecimal productDiscount,
                                   BigDecimal voucherDiscount,
                                   BigDecimal shippingFee,
                                   BigDecimal shippingDiscount) {
        BigDecimal ship = shippingFee.subtract(shippingDiscount).max(BigDecimal.ZERO);
        BigDecimal goods = subtotal.subtract(productDiscount).subtract(voucherDiscount)
                                   .max(BigDecimal.ZERO);
        return goods.add(ship);
    }
}