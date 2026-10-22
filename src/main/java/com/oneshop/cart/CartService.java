package com.oneshop.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Cart;
import com.oneshop.entity.CartItem;
import com.oneshop.entity.Product;
import com.oneshop.entity.User;
import com.oneshop.repository.CartItemRepository;
import com.oneshop.repository.CartRepository;
import com.oneshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    public record Line(Long itemId, Long productId, String name,
                       BigDecimal price, int quantity, int stock) {}
    public record ShopGroup(Long shopId, String shopName, List<Line> lines) {}

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;

    private Cart cartOf(User user) {
        return cartRepo.findByUserId(user.getId())
                .orElseGet(() -> cartRepo.save(Cart.builder().user(user).build()));
    }

    private void checkStock(Product product, int quantity) {
        if (product.getStock() <= 0) {
            throw new IllegalStateException("Sản phẩm đã hết hàng");
        }
        if (quantity > product.getStock()) {
            throw new IllegalStateException("Chỉ còn " + product.getStock() + " sản phẩm trong kho");
        }
    }

    @Transactional
    public void add(User user, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải từ 1 trở lên");
        }
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));
        if (!product.isActive() || product.isLocked()
                || product.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new IllegalStateException("Sản phẩm hiện không còn được bán");
        }
        if (product.getShop().getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không thể mua sản phẩm của chính shop mình");
        }

        Cart cart = cartOf(user);
        CartItem item = itemRepo.findByCartIdAndProductId(cart.getId(), productId).orElse(null);
        int newQty = (item == null ? 0 : item.getQuantity()) + quantity;
        checkStock(product, newQty);

        if (item == null) {
            item = CartItem.builder().cart(cart).product(product).quantity(newQty).build();
        } else {
            item.setQuantity(newQty);
        }
        itemRepo.save(item);
    }

    @Transactional
    public void updateQuantity(User user, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải từ 1 trở lên");
        }
        CartItem item = itemRepo.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        itemRepo.save(item);
    }

    @Transactional
    public void remove(User user, Long itemId) {
        CartItem item = itemRepo.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        itemRepo.delete(item);
    }

    @Transactional(readOnly = true)
    public long count(User user) {
        return itemRepo.totalQuantity(user.getId());
    }

    @Transactional(readOnly = true)
    public List<ShopGroup> view(User user) {
        Map<Long, ShopGroup> groups = new LinkedHashMap<>();
        for (CartItem i : itemRepo.findDetailByUserId(user.getId())) {
            Product p = i.getProduct();
            groups.computeIfAbsent(p.getShop().getId(),
                            id -> new ShopGroup(id, p.getShop().getName(), new ArrayList<>()))
                    .lines()
                    .add(new Line(i.getId(), p.getId(), p.getName(), p.getPrice(),
                            i.getQuantity(), p.getStock()));
        }
        return new ArrayList<>(groups.values());
    }
}