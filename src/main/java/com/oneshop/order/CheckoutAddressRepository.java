package com.oneshop.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.Address;

public interface CheckoutAddressRepository extends JpaRepository<Address, Long> {

    @Query("select a from Address a where a.user.email = :email "
         + "order by a.defaultAddress desc, a.id asc")
    List<Address> listForUser(@Param("email") String email);

    Optional<Address> findByIdAndUserId(Long id, Long userId);
}