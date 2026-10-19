package com.oneshop.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.Payment;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;

class VnpayServiceTests {
    private final OrderRepository orders = mock(OrderRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final VnpayService service = new VnpayService(orders, payments);

    @BeforeEach
    void configure() {
        ReflectionTestUtils.setField(service, "terminalCode", "TEST1234");
        ReflectionTestUtils.setField(service, "hashSecret", "sample-secret-for-tests");
        ReflectionTestUtils.setField(service, "returnUrl", "http://localhost:8080/payment/vnpay/return");
        ReflectionTestUtils.setField(service, "payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
    }

    @Test
    void createsSignedUrlAndAcceptsOnlyMatchingCallbackOnce() {
        Order order = Order.builder().paymentMethod(PaymentMethod.VNPAY)
                .total(new BigDecimal("120000")).build();
        when(orders.findByIdAndUserId(7L, 9L)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(7L)).thenReturn(Optional.empty());
        String url = service.createUrl(7L, 9L, "127.0.0.1");
        assertTrue(url.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        Map<String, String> callback = Arrays.stream(url.substring(url.indexOf('?') + 1).split("&"))
                .map(part -> part.split("=", 2))
                .collect(Collectors.toMap(part -> URLDecoder.decode(part[0], StandardCharsets.UTF_8),
                        part -> URLDecoder.decode(part[1], StandardCharsets.UTF_8)));
        assertEquals("12000000", callback.get("vnp_Amount"));
        assertTrue(service.validSignature(callback));

        Payment payment = Payment.builder().order(order).method(PaymentMethod.VNPAY)
                .amount(new BigDecimal("120000")).reference(callback.get("vnp_TxnRef")).build();
        when(payments.findLockedByOrderId(7L)).thenReturn(Optional.of(payment));
        callback.put("vnp_ResponseCode", "00");
        callback.put("vnp_TransactionStatus", "00");
        callback.put("vnp_TransactionNo", "12345");
        callback.put("vnp_SecureHash", VnpayService.hmac("sample-secret-for-tests", VnpayService.canonical(
                callback.entrySet().stream().filter(entry -> !entry.getKey().equals("vnp_SecureHash"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))));
        assertEquals("00", service.processCallback(callback).code());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals("02", service.processCallback(callback).code());
        callback.put("vnp_Amount", "1");
        assertEquals("97", service.processCallback(callback).code());
        verify(payments).save(any(Payment.class));
    }
}
