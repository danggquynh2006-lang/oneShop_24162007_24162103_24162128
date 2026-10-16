package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByDefaultAddressDescIdDesc(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    List<Address> findByUserIdAndDefaultAddressTrue(Long userId);
    long countByUserId(Long userId);
}