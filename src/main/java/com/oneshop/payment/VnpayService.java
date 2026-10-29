package com.oneshop.payment;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.Payment;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class VnpayService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${vnpay.tmn-code:}") private String terminalCode;
    @Value("${vnpay.hash-secret:}") private String hashSecret;
    @Value("${vnpay.return-url:}") private String returnUrl;
    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") private String payUrl;

    public record Result(String code, String message, boolean paid) {}

    @Transactional
    public String createUrl(Long orderId, Long userId, String ipAddress) {
        if (terminalCode.isBlank() || hashSecret.isBlank() || returnUrl.isBlank())
            throw new BusinessException("Chưa cấu hình VNPAY sandbox.");
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng."));
        if (order.getPaymentMethod() != PaymentMethod.VNPAY)
            throw new BusinessException("Đơn hàng không chọn VNPAY.");
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED)
            throw new BusinessException("Đơn hàng không còn chờ thanh toán.");
        Payment payment = paymentRepository.findByOrderId(orderId).orElseGet(() ->
                Payment.builder().order(order).method(PaymentMethod.VNPAY).amount(order.getTotal()).build());
        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED)
            throw new BusinessException("Đơn hàng đã thanh toán.");
        String reference = orderId + "-" + UUID.randomUUID().toString().substring(0, 8);
        payment.setReference(reference);
        payment.setAmount(order.getTotal());
        payment.setStatus(PaymentStatus.UNPAID);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        payment.setRequestDate(now.format(formatter));
        paymentRepository.save(payment);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_Amount", order.getTotal().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", reference);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", payment.getRequestDate());
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));
        String query = canonical(params);
        return payUrl + "?" + query + "&vnp_SecureHash=" + hmac(hashSecret, query);
    }

    @Transactional
    public Result processCallback(Map<String, String> params) {
        if (!validSignature(params)) return new Result("97", "Chữ ký không hợp lệ", false);
        if (!terminalCode.equals(params.get("vnp_TmnCode")))
            return new Result("97", "Mã merchant không hợp lệ", false);
        String reference = params.get("vnp_TxnRef");
        if (reference == null || !reference.matches("[0-9]+-[a-f0-9]{8}"))
            return new Result("01", "Mã đơn không hợp lệ", false);
        Long orderId = Long.valueOf(reference.substring(0, reference.indexOf('-')));
        Payment payment = paymentRepository.findLockedByOrderId(orderId).orElse(null);
        if (payment == null || !reference.equals(payment.getReference()))
            return new Result("01", "Không tìm thấy giao dịch", false);
        if (!payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString()
                .equals(params.get("vnp_Amount")))
            return new Result("04", "Số tiền không khớp", false);
        if (payment.getStatus() != PaymentStatus.UNPAID)
            return new Result("02", "Giao dịch đã xử lý", payment.getStatus() == PaymentStatus.PAID);
        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
        payment.setStatus(success ? (payment.getOrder().getStatus() == OrderStatus.CANCELLED
                ? PaymentStatus.REFUND_PENDING : PaymentStatus.PAID) : PaymentStatus.FAILED);
        if (success) {
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionCode(params.get("vnp_TransactionNo"));
            payment.setTransactionDate(params.get("vnp_PayDate"));
        }
        return new Result("00", success ? "Thanh toán thành công" : "Thanh toán không thành công", success);
    }

    public boolean validSignature(Map<String, String> params) {
        String signature = params.get("vnp_SecureHash");
        if (signature == null || hashSecret.isBlank()) return false;
        Map<String, String> signed = new TreeMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("vnp_") && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")) signed.put(key, value);
        });
        byte[] expected = hmac(hashSecret, canonical(signed)).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = signature.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    static String canonical(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        new TreeMap<>(params).forEach((key, value) -> {
            if (value == null || value.isEmpty()) return;
            if (!query.isEmpty()) query.append('&');
            query.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            query.append('=');
            query.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        });
        return query.toString();
    }

    static String hmac(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể ký yêu cầu VNPAY.", exception);
        }
    }
}
