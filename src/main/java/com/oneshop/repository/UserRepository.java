package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.oneshop.common.Role;
import com.oneshop.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Danh sách shipper đang hoạt động, dùng khi Admin phân công đơn. */
    List<User> findByRoleAndEnabledTrueAndLockedFalseOrderByEmailAsc(Role role);
}