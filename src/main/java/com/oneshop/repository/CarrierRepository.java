package com.oneshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.entity.Carrier;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
}