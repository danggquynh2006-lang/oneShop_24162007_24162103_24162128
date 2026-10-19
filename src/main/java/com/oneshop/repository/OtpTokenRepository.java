package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.common.OtpType;
import com.oneshop.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    List<OtpToken> findByEmailAndTypeAndUsedFalse(String email, OtpType type);
    Optional<OtpToken> findFirstByEmailAndTypeAndUsedFalseOrderByIdDesc(String email, OtpType type);
}