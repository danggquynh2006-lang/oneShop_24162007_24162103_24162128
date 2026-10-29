package com.oneshop.home;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.ProductImage;

public interface HomeImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("select i.product.id, i.url from ProductImage i "
         + "where i.product.id in :ids order by i.mainImage desc, i.id asc")
    List<Object[]> findUrls(@Param("ids") Collection<Long> ids);
}