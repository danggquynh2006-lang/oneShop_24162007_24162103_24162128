package com.oneshop.entity;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductPriceTests {

    private Product product(String price, int discountPercent) {
        return Product.builder().price(new BigDecimal(price)).discountPercent(discountPercent).build();
    }

    @Test
    void noDiscountKeepsOriginalPrice() {
        assertEquals(0, new BigDecimal("289000").compareTo(product("289000", 0).getFinalPrice()));
    }

    @Test
    void discountIsAppliedAsPercentage() {
        assertEquals(0, new BigDecimal("231200").compareTo(product("289000", 20).getFinalPrice()));
        assertEquals(0, new BigDecimal("152150").compareTo(product("179000", 15).getFinalPrice()));
    }

    @Test
    void finalPriceIsRoundedToWholeDong() {
        assertEquals(0, new BigDecimal("66999").compareTo(product("99999", 33).getFinalPrice()));
    }

    @Test
    void discountOverOneHundredNeverGoesNegative() {
        assertEquals(0, BigDecimal.ZERO.compareTo(product("100000", 150).getFinalPrice()));
    }

    @Test
    void missingPriceReturnsNull() {
        assertNull(Product.builder().discountPercent(10).build().getFinalPrice());
    }
}