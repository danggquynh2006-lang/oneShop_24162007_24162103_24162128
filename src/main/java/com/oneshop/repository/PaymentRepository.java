package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import com.oneshop.entity.Payment;
import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findLockedByOrderId(Long orderId);
}
