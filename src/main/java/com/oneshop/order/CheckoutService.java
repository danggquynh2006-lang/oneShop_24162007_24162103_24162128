package com.oneshop.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.oneshop.common.BusinessException;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Address;
import com.oneshop.entity.Carrier;
import com.oneshop.entity.CartItem;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.Product;
import com.oneshop.entity.Shop;
import com.oneshop.entity.User;
import com.oneshop.promotion.PromotionService;
import com.oneshop.repository.CartItemRepository;
import com.oneshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    public record Line(Long productId, String name, BigDecimal price, int quantity,
                       BigDecimal lineTotal) {}
    public record ShopLines(Long shopId, String shopName, List<Line> lines, BigDecimal subtotal) {}
    public record Preview(List<ShopLines> shops, BigDecimal subtotal) {}
    public record OrderRef(Long id, String shopName, BigDecimal total) implements Serializable {}
    public record Result(String groupCode, List<OrderRef> orders, BigDecimal total,
                         PaymentMethod method) implements Serializable {}
    public record Quote(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal discount,
                        BigDecimal total, String message) {}

    private final CartItemRepository itemRepo;
    private final CheckoutAddressRepository addressRepo;
    private final CheckoutCarrierRepository carrierRepo;
    private final CheckoutProductRepository productRepo;
    private final CheckoutOrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;
    private final PromotionService promotionService;
    private final TransactionTemplate tx;


    public Preview preview(User user, Collection<Long> itemIds) {
        return tx.execute(status -> buildPreview(user, itemIds));
    }

    public Quote quote(User user, List<Long> itemIds, Long carrierId, String voucher) {
        Carrier carrier = carrierRepo.findById(carrierId).filter(Carrier::isActive)
                .orElseThrow(() -> new BusinessException("Đơn vị vận chuyển không hợp lệ."));
        Preview preview = preview(user, itemIds);
        BigDecimal fee = carrier.getShippingFee();
        BigDecimal shippingTotal = fee.multiply(BigDecimal.valueOf(preview.shops().size()));

        BigDecimal productDiscount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        String message = "";

        if (voucher != null && !voucher.isBlank()) {
            String code = voucher.trim();
            String firstError = null;
            int applied = 0;
            for (ShopLines s : preview.shops()) {
                try {
                    PromotionService.Discount d =
                            promotionService.calculate(code, s.shopId(), s.subtotal(), fee);
                    productDiscount = productDiscount.add(d.productDiscount());
                    shippingDiscount = shippingDiscount.add(d.shippingDiscount());
                    applied++;
                } catch (BusinessException e) {
                    if (firstError == null) firstError = e.getMessage();
                }
            }
            if (applied == 0) {
                throw new BusinessException(firstError != null ? firstError : "Mã giảm giá không hợp lệ.");
            }
            message = "Áp dụng thành công cho " + applied + "/" + preview.shops().size() + " shop.";
        }

        BigDecimal total = PriceCalculator.total(preview.subtotal(), productDiscount,
                BigDecimal.ZERO, shippingTotal, shippingDiscount);
        return new Quote(preview.subtotal(), shippingTotal,
                productDiscount.add(shippingDiscount), total, message);
    }


    public Result place(User user, List<Long> itemIds, Long addressId, Long carrierId,
                        PaymentMethod method, String voucher) {
        if (method != PaymentMethod.COD && method != PaymentMethod.VNPAY) {
            throw new BusinessException("Phương thức thanh toán này hiện chưa được hỗ trợ.");
        }
        Carrier carrier = carrierRepo.findById(carrierId).filter(Carrier::isActive)
                .orElseThrow(() -> new BusinessException("Đơn vị vận chuyển không hợp lệ."));
        Address address = addressRepo.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new BusinessException("Vui lòng chọn địa chỉ nhận hàng."));

        Preview preview = preview(user, itemIds);
        Set<Long> discountedShops = eligibleShops(voucher, preview, carrier.getShippingFee());

        return tx.execute(status ->
                create(user, itemIds, address, carrier, method, voucher, discountedShops));
    }

    private Set<Long> eligibleShops(String voucher, Preview preview, BigDecimal fee) {
        Set<Long> ok = new HashSet<>();
        if (voucher == null || voucher.isBlank()) {
            return ok;
        }
        String code = voucher.trim();
        String firstError = null;
        for (ShopLines s : preview.shops()) {
            try {
                promotionService.calculate(code, s.shopId(), s.subtotal(), fee);
                ok.add(s.shopId());
            } catch (BusinessException e) {
                if (firstError == null) firstError = e.getMessage();
            }
        }
        if (ok.isEmpty()) {
            throw new BusinessException(firstError != null ? firstError : "Mã giảm giá không hợp lệ.");
        }
        return ok;
    }

    private Result create(User user, List<Long> itemIds, Address address, Carrier carrier,
                          PaymentMethod method, String voucher, Set<Long> discountedShops) {
        List<CartItem> items = selected(user, itemIds);

        for (CartItem i : items) {
            Product p = i.getProduct();
            if (!p.isActive() || p.isLocked() || p.getShop().getStatus() != ShopStatus.ACTIVE) {
                throw new BusinessException("Sản phẩm \"" + p.getName() + "\" hiện không còn được bán.");
            }
            if (p.getShop().getOwner().getId().equals(user.getId())) {
                throw new BusinessException("Bạn không thể mua sản phẩm của chính shop mình.");
            }
        }

        List<CartItem> ordered = new ArrayList<>(items);
        ordered.sort(Comparator.comparing(i -> i.getProduct().getId()));
        for (CartItem i : ordered) {
            if (productRepo.decreaseStock(i.getProduct().getId(), i.getQuantity()) == 0) {
                throw new BusinessException(
                        "Sản phẩm \"" + i.getProduct().getName() + "\" không đủ hàng trong kho.");
            }
        }

        String snapshot = snapshot(address);
        String groupCode = "G" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        List<OrderRef> refs = new ArrayList<>();
        BigDecimal grand = BigDecimal.ZERO;

        for (List<CartItem> lines : groupByShop(items).values()) {
            Shop shop = lines.get(0).getProduct().getShop();
            BigDecimal subtotal = lines.stream()
                    .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal fee = carrier.getShippingFee();

            BigDecimal productDiscount = BigDecimal.ZERO;
            BigDecimal shippingDiscount = BigDecimal.ZERO;
            if (discountedShops.contains(shop.getId())) {
                PromotionService.Discount d =
                        promotionService.redeem(voucher.trim(), shop.getId(), subtotal, fee);
                productDiscount = d.productDiscount();
                shippingDiscount = d.shippingDiscount();
            }

            BigDecimal total = PriceCalculator.total(
                    subtotal, productDiscount, BigDecimal.ZERO, fee, shippingDiscount);

            Order order = orderRepo.save(Order.builder()
                    .user(user)
                    .shop(shop)
                    .carrier(carrier)
                    .addressSnapshot(snapshot)
                    .subtotal(subtotal)
                    .discount(productDiscount.add(shippingDiscount))
                    .shippingFee(fee)
                    .total(total)
                    .paymentMethod(method)
                    .paymentGroupCode(groupCode)
                    .build());

            for (CartItem i : lines) {
                Product p = i.getProduct();
                orderItemRepo.save(OrderItem.builder()
                        .order(order)
                        .product(p)
                        .productName(p.getName())
                        .unitPrice(p.getPrice())
                        .quantity(i.getQuantity())
                        .build());
            }
            refs.add(new OrderRef(order.getId(), shop.getName(), total));
            grand = grand.add(total);
        }

        itemRepo.deleteAll(items);
        return new Result(groupCode, refs, grand, method);
    }


    private List<CartItem> selected(User user, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("Bạn chưa chọn sản phẩm nào để thanh toán.");
        }
        Set<Long> wanted = new HashSet<>(ids);
        List<CartItem> items = itemRepo.findDetailByUserId(user.getId()).stream()
                .filter(i -> wanted.contains(i.getId()))
                .toList();
        if (items.size() != wanted.size()) {
            throw new BusinessException(
                    "Giỏ hàng đã thay đổi, vui lòng quay lại giỏ hàng và chọn lại.");
        }
        return items;
    }

    private Map<Long, List<CartItem>> groupByShop(List<CartItem> items) {
        Map<Long, List<CartItem>> byShop = new LinkedHashMap<>();
        for (CartItem i : items) {
            byShop.computeIfAbsent(i.getProduct().getShop().getId(), k -> new ArrayList<>()).add(i);
        }
        return byShop;
    }

    private Preview buildPreview(User user, Collection<Long> itemIds) {
        List<CartItem> items = selected(user, itemIds);
        List<ShopLines> shops = new ArrayList<>();
        BigDecimal all = BigDecimal.ZERO;

        for (Map.Entry<Long, List<CartItem>> e : groupByShop(items).entrySet()) {
            List<Line> lines = new ArrayList<>();
            BigDecimal sub = BigDecimal.ZERO;
            for (CartItem i : e.getValue()) {
                Product p = i.getProduct();
                BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                lines.add(new Line(p.getId(), p.getName(), p.getPrice(), i.getQuantity(), lineTotal));
                sub = sub.add(lineTotal);
            }
            String shopName = e.getValue().get(0).getProduct().getShop().getName();
            shops.add(new ShopLines(e.getKey(), shopName, lines, sub));
            all = all.add(sub);
        }
        return new Preview(shops, all);
    }

    private String snapshot(Address a) {
        String who = a.getReceiverName() + " · " + a.getPhone();
        String where = List.of(nz(a.getDetail()), nz(a.getWard()), nz(a.getProvince())).stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        return who + ", " + where;
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
}