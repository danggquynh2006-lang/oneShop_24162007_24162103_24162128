package com.oneshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long ownerId);
    Optional<Shop> findByIdAndStatus(Long id, ShopStatus status);
    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    // ===== Phần thêm cho Admin/Manager (TV1) =====

    /** Tìm shop theo tên shop hoặc email chủ shop, mọi trạng thái. */
    @EntityGraph(attributePaths = "owner")
    @Query("select s from Shop s where lower(s.name) like lower(concat('%', :keyword, '%')) "
            + "or lower(s.owner.email) like lower(concat('%', :keyword, '%'))")
    Page<Shop> searchAll(@Param("keyword") String keyword, Pageable pageable);

    /** Như trên nhưng lọc thêm theo trạng thái. */
    @EntityGraph(attributePaths = "owner")
    @Query("select s from Shop s where s.status = :status and (lower(s.name) like lower(concat('%', :keyword, '%')) "
            + "or lower(s.owner.email) like lower(concat('%', :keyword, '%')))")
    Page<Shop> searchByStatus(@Param("status") ShopStatus status, @Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ShopStatus status);
}