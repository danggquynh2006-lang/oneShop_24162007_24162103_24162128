package com.oneshop.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VnpayAvailability {

    @Value("${vnpay.tmn-code:}")
    private String terminalCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.return-url:}")
    private String returnUrl;

    public boolean enabled() {
        return !terminalCode.isBlank() && !hashSecret.isBlank() && !returnUrl.isBlank();
    }
}