package com.oneshop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.Product;

public interface CheckoutProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("update Product p set p.stock = p.stock - :qty where p.id = :id and p.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);
    
    @Modifying
    @Query("update Product p set p.stock = p.stock + :qty where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);
}