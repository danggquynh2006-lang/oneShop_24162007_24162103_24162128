package com.oneshop.vendor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.Product;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;
import com.oneshop.repository.ShopRepository;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.Payment;

@SpringBootTest @ActiveProfiles("demo") @Transactional
class VendorOrderFlowTests {
    @Autowired private VendorOrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository itemRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ShopRepository shopRepository;

    private Long demoShopId() {
        return shopRepository.findAll().stream()
                .filter(shop -> shop.getName().equals("Lumière Beauty")).findFirst().orElseThrow().getId();
    }

    private Order orderWithStatus(OrderStatus status) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == status).findFirst().orElseThrow();
    }

    private Product firstProductOf(Order order) {
        return itemRepository.findByOrderId(order.getId()).get(0).getProduct();
    }

    @Test
    void confirmMovesNewOrderToConfirmed() {
        Order order = orderWithStatus(OrderStatus.NEW);
        orderService.confirm(demoShopId(), order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void confirmTwiceIsRejected() {
        Order order = orderWithStatus(OrderStatus.NEW);
        Long shopId = demoShopId();
        orderService.confirm(shopId, order.getId());
        assertThrows(BusinessException.class, () -> orderService.confirm(shopId, order.getId()));
    }
    @Test
    void rejectCancelsOrderAndRestoresStock() {
        Order order = orderWithStatus(OrderStatus.NEW);
        Product product = firstProductOf(order);
        int quantity = itemRepository.findByOrderId(order.getId()).get(0).getQuantity();
        int stockBefore = product.getStock();

        orderService.reject(demoShopId(), order.getId());

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(stockBefore + quantity, product.getStock());
    }

    @Test
    void rejectOnlyWorksForNewOrders() {
        Order delivered = orderWithStatus(OrderStatus.DELIVERED);
        Long shopId = demoShopId();
        assertThrows(BusinessException.class, () -> orderService.reject(shopId, delivered.getId()));
    }
    @Test
    void acceptedReturnRestoresStockAndStartsRefund() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        Product product = firstProductOf(order);
        int quantity = itemRepository.findByOrderId(order.getId()).get(0).getQuantity();
        int stockBefore = product.getStock();
        int soldBefore = product.getSold();

        orderService.resolveReturn(demoShopId(), order.getId(), true);

        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(stockBefore + quantity, product.getStock());
        assertEquals(soldBefore - quantity, product.getSold());
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow();
        assertEquals(PaymentStatus.REFUND_PENDING, payment.getStatus());
        assertNotNull(payment.getRefundRequestedAt());
    }

    @Test
    void rejectedReturnKeepsOrderDelivered() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        Product product = firstProductOf(order);
        int stockBefore = product.getStock();

        orderService.resolveReturn(demoShopId(), order.getId(), false);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(stockBefore, product.getStock());
    }
    @Test
    void cashRefundCompletesAfterAcceptedReturn() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        Long shopId = demoShopId();
        orderService.resolveReturn(shopId, order.getId(), true);

        orderService.completeCashRefund(shopId, order.getId());

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        assertEquals(PaymentStatus.REFUNDED, paymentRepository.findByOrderId(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void cashRefundIsRefusedBeforeReturn() {
        Order order = orderWithStatus(OrderStatus.DELIVERED);
        Long shopId = demoShopId();
        assertThrows(BusinessException.class, () -> orderService.completeCashRefund(shopId, order.getId()));
    }
    @Test
    void markDeliveredUpdatesSoldCommissionAndCodPayment() {
        Order order = orderWithStatus(OrderStatus.NEW);
        order.setStatus(OrderStatus.SHIPPING);
        Product product = firstProductOf(order);
        int quantity = itemRepository.findByOrderId(order.getId()).get(0).getQuantity();
        int soldBefore = product.getSold();

        orderService.markDelivered(order);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getDeliveredAt());
        assertEquals(0, order.getShop().getCommissionRate().compareTo(order.getCommissionRateSnapshot()));
        assertEquals(soldBefore + quantity, product.getSold());
        assertEquals(PaymentStatus.PAID, paymentRepository.findByOrderId(order.getId()).orElseThrow().getStatus());
    }

    @Test
    void markDeliveredRequiresShippingStatus() {
        Order order = orderWithStatus(OrderStatus.NEW);
        assertThrows(BusinessException.class, () -> orderService.markDelivered(order));
    }
}