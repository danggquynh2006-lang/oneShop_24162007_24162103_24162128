package com.oneshop.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.Carrier;

public interface CheckoutCarrierRepository extends JpaRepository<Carrier, Long> {

    List<Carrier> findByActiveTrueOrderByShippingFeeAsc();
}