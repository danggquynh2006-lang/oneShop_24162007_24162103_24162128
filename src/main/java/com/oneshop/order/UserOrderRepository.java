package com.oneshop.order;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;

public interface UserOrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.shop "
         + "where o.user.id = :userId and o.status in :statuses order by o.createdAt desc")
    List<Order> findByTab(@Param("userId") Long userId,
                          @Param("statuses") Collection<OrderStatus> statuses);

    @Query("select o from Order o join fetch o.shop left join fetch o.carrier "
         + "where o.id = :id and o.user.id = :userId")
    Optional<Order> findDetail(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("update Order o set o.status = :to where o.id = :id and o.user.id = :userId "
         + "and o.status = :from")
    int changeStatus(@Param("id") Long id, @Param("userId") Long userId,
                     @Param("from") OrderStatus from, @Param("to") OrderStatus to);
}