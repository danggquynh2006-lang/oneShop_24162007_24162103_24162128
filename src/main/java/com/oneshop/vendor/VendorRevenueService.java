package com.oneshop.vendor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;
import com.oneshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class VendorRevenueService {
    private final OrderRepository orderRepository;

    public record Point(String label, BigDecimal gross, BigDecimal net, int orders) {}
    public record Summary(BigDecimal gross, BigDecimal commission, BigDecimal net, int orders, List<Point> points) {}

    @Transactional(readOnly = true)
    public Summary summary(Long shopId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        List<Order> orders = orderRepository.findByShopIdAndStatusAndDeliveredAtBetween(
                shopId, OrderStatus.DELIVERED, start.atStartOfDay(), end.atStartOfDay().minusNanos(1));
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;
        List<Point> points = new ArrayList<>();
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            BigDecimal dayGross = BigDecimal.ZERO;
            BigDecimal dayNet = BigDecimal.ZERO;
            int dayOrders = 0;
            for (Order order : orders) {
                if (!order.getDeliveredAt().toLocalDate().equals(date)) continue;
                BigDecimal sale = order.getSubtotal().subtract(order.getDiscount());
                BigDecimal rate = order.getCommissionRateSnapshot() == null
                        ? order.getShop().getCommissionRate() : order.getCommissionRateSnapshot();
                BigDecimal fee = sale.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                gross = gross.add(sale);
                commission = commission.add(fee);
                dayGross = dayGross.add(sale);
                dayNet = dayNet.add(sale.subtract(fee));
                dayOrders++;
            }
            points.add(new Point(String.valueOf(date.getDayOfMonth()), dayGross, dayNet, dayOrders));
        }
        return new Summary(gross, commission, gross.subtract(commission), orders.size(), points);
    }

    @Transactional(readOnly = true)
    public Summary annual(Long shopId, int year) {
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;
        int count = 0;
        List<Point> points = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Summary current = summary(shopId, year, month);
            points.add(new Point("Th " + month, current.gross(), current.net(), current.orders()));
            gross = gross.add(current.gross());
            commission = commission.add(current.commission());
            count += current.orders();
        }
        return new Summary(gross, commission, gross.subtract(commission), count, points);
    }

    @Transactional(readOnly = true)
    public Summary daily(Long shopId, LocalDate date) {
        List<Order> orders = orderRepository.findByShopIdAndStatusAndDeliveredAtBetween(shopId,
                OrderStatus.DELIVERED, date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1));
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal commission = BigDecimal.ZERO;
        List<Point> points = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            BigDecimal hourGross = BigDecimal.ZERO;
            BigDecimal hourNet = BigDecimal.ZERO;
            int count = 0;
            for (Order order : orders) {
                if (order.getDeliveredAt().getHour() != hour) continue;
                BigDecimal sale = order.getSubtotal().subtract(order.getDiscount());
                BigDecimal rate = order.getCommissionRateSnapshot() == null
                        ? order.getShop().getCommissionRate() : order.getCommissionRateSnapshot();
                BigDecimal fee = sale.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                gross = gross.add(sale);
                commission = commission.add(fee);
                hourGross = hourGross.add(sale);
                hourNet = hourNet.add(sale.subtract(fee));
                count++;
            }
            points.add(new Point(hour + "h", hourGross, hourNet, count));
        }
        return new Summary(gross, commission, gross.subtract(commission), orders.size(), points);
    }
}
