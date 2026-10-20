package com.oneshop.payment;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.Payment;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class VnpayRefundService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Value("${vnpay.tmn-code:}") private String terminalCode;
    @Value("${vnpay.hash-secret:}") private String hashSecret;
    @Value("${vnpay.refund-url:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}") private String refundUrl;

    @Transactional
    public void refund(Long shopId, Long orderId, String actor, String ipAddress) {
        Order order = orderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng của shop."));
        Payment payment = paymentRepository.findLockedByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch thanh toán."));
        if ((order.getStatus() != OrderStatus.RETURNED && order.getStatus() != OrderStatus.CANCELLED)
                || payment.getStatus() != PaymentStatus.REFUND_PENDING)
            throw new BusinessException("Đơn chưa đủ điều kiện hoàn tiền.");
        if (terminalCode.isBlank() || hashSecret.isBlank()) throw new BusinessException("Chưa cấu hình VNPAY sandbox.");
        if (payment.getReference() == null || payment.getRequestDate() == null || payment.getTransactionCode() == null)
            throw new BusinessException("Thiếu thông tin giao dịch gốc để hoàn tiền.");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", UUID.nameUUIDFromBytes(
                (payment.getReference() + "-full-refund").getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", ""));
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_TransactionType", "02");
        params.put("vnp_TxnRef", payment.getReference());
        params.put("vnp_Amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_TransactionNo", payment.getTransactionCode());
        params.put("vnp_TransactionDate", payment.getRequestDate());
        params.put("vnp_CreateBy", actor);
        params.put("vnp_CreateDate", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DATE));
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_OrderInfo", "Hoan tien don hang " + orderId);
        params.put("vnp_SecureHash", VnpayService.hmac(hashSecret, String.join("|", params.values())));

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(refundUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(params)))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new BusinessException("VNPAY chưa chấp nhận yêu cầu hoàn tiền.");
            Map<String, String> body = objectMapper.readValue(response.body(), new TypeReference<>() {});
            if (!validResponse(body, payment)) throw new BusinessException("Phản hồi hoàn tiền VNPAY không hợp lệ.");
            if (!"00".equals(body.get("vnp_ResponseCode")) || !"00".equals(body.get("vnp_TransactionStatus")))
                throw new BusinessException("VNPAY chưa hoàn tiền: " + body.getOrDefault("vnp_Message", "Không rõ lỗi"));
            payment.setStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("Không kết nối được VNPAY. Hãy thử lại sau.");
        }
    }

    private boolean validResponse(Map<String, String> response, Payment payment) {
        if (!terminalCode.equals(response.get("vnp_TmnCode"))
                || !payment.getReference().equals(response.get("vnp_TxnRef"))
                || !payment.getAmount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString()
                        .equals(response.get("vnp_Amount"))) return false;
        String[] keys = {"vnp_ResponseId", "vnp_Command", "vnp_ResponseCode", "vnp_Message", "vnp_TmnCode",
                "vnp_TxnRef", "vnp_Amount", "vnp_BankCode", "vnp_PayDate", "vnp_TransactionNo",
                "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo"};
        String[] values = new String[keys.length];
        for (int index = 0; index < keys.length; index++) values[index] = response.getOrDefault(keys[index], "");
        String signature = response.get("vnp_SecureHash");
        return signature != null && MessageDigest.isEqual(
                VnpayService.hmac(hashSecret, String.join("|", values)).getBytes(StandardCharsets.US_ASCII),
                signature.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }
}
