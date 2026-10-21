package com.oneshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.oneshop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);
    Page<Product> findByShopIdAndActiveTrue(Long shopId, Pageable pageable);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
}