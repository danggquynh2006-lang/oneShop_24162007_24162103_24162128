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
}