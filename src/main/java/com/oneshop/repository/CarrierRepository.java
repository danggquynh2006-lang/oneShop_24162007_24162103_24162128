package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.entity.Carrier;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    Page<Carrier> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Carrier> findFirstByNameIgnoreCase(String name);

    long countByActiveTrue();

    /** Nhà vận chuyển đang hoạt động, dùng ở trang thanh toán. */
    List<Carrier> findByActiveTrueOrderByNameAsc();
}