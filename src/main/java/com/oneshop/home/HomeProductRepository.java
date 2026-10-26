package com.oneshop.home;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;

public interface HomeProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    String VISIBLE = " from Product p join p.shop s join p.category c left join c.parent cp "
            + "where p.active = true and (p.locked is null or p.locked = false) "
            + "and s.status = :status and c.active = true "
            + "and (cp is null or cp.active = true) ";

    @Query(value = "select p" + VISIBLE + "and p.sold > :minSold",
           countQuery = "select count(p)" + VISIBLE + "and p.sold > :minSold")
    Page<Product> findVisible(@Param("minSold") int minSold,
                              @Param("status") ShopStatus status,
                              Pageable pageable);

    @Query("select p from Product p join fetch p.shop s join fetch p.category c "
         + "left join c.parent cp "
         + "where p.id = :id and p.active = true and (p.locked is null or p.locked = false) "
         + "and s.status = :status and c.active = true "
         + "and (cp is null or cp.active = true)")
    Optional<Product> findVisibleById(@Param("id") Long id, @Param("status") ShopStatus status);

    @Query("select p from Product p join p.shop s "
         + "where p.category.id = :categoryId and p.id <> :id and p.active = true "
         + "and (p.locked is null or p.locked = false) "
         + "and s.status = :status order by p.sold desc")
    List<Product> findRelated(@Param("categoryId") Long categoryId,
                              @Param("id") Long id,
                              @Param("status") ShopStatus status,
                              Pageable pageable);

    @Query("select p" + VISIBLE
         + "order by (select count(f) from Favorite f where f.product = p) desc, p.sold desc, p.id asc")
    List<Product> findMostLoved(@Param("status") ShopStatus status, Pageable pageable);

    @Query("select distinct p.brand" + VISIBLE
         + "and p.brand is not null and p.brand <> '' order by p.brand")
    List<String> findBrands(@Param("status") ShopStatus status);

    @Query("select distinct s.id, s.name" + VISIBLE + "order by s.name")
    List<Object[]> findShops(@Param("status") ShopStatus status);
}