package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.ReturnRequest;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByOrderId(Long orderId);
}