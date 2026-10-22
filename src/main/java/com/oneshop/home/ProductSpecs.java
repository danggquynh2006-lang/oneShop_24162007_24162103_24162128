package com.oneshop.home;

import java.math.BigDecimal;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.Shop;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class ProductSpecs {

    private ProductSpecs() {}

    public static Specification<Product> visible() {
        return (root, query, cb) -> {
            Join<Product, Shop> shop = root.join("shop");
            Join<Product, Category> category = root.join("category");
            Join<Category, Category> parent = category.join("parent", JoinType.LEFT);
            return cb.and(
                    cb.isTrue(root.<Boolean>get("active")),
                    cb.or(cb.isNull(root.<Boolean>get("locked")),
                          cb.isFalse(root.<Boolean>get("locked"))),
                    cb.equal(shop.get("status"), ShopStatus.ACTIVE),
                    cb.isTrue(category.<Boolean>get("active")),
                    cb.or(cb.isNull(parent.get("id")), cb.isTrue(parent.<Boolean>get("active"))));
        };
    }

    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String escaped = keyword.trim().toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%")
                    .replace("_", "\\_").replace("[", "\\[");
            return cb.like(cb.lower(root.<String>get("name")), "%" + escaped + "%", '\\');
        };
    }

    public static Specification<Product> inCategories(Collection<Long> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty()
                ? cb.conjunction()
                : root.get("category").get("id").in(ids);
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (min != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), min));
            }
            if (max != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), max));
            }
            return predicate;
        };
    }

    public static Specification<Product> inShop(Long shopId) {
        return (root, query, cb) -> shopId == null
                ? cb.conjunction()
                : cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Product> brandIs(String brand) {
        return (root, query, cb) -> brand == null || brand.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.<String>get("brand")), brand.trim().toLowerCase());
    }

    public static Specification<Product> ratingAtLeast(Double min) {
        return (root, query, cb) -> min == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.<Double>get("avgRating"), min);
    }
}