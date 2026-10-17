package com.oneshop.admin;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.Role;
import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int PAGE_SIZE = 10;

    private final UserRepository userRepository;

    /** Tìm kiếm theo từ khóa (email, họ tên, SĐT), vai trò, trạng thái; có phân trang. */
    @Transactional(readOnly = true)
    public Page<User> search(String keyword, Role role, String status, int page) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("email")), like),
                    cb.like(cb.lower(root.<String>get("fullName")), like),
                    cb.like(root.<String>get("phone"), like)));
        }
        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if ("locked".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.<Boolean>get("locked")));
        } else if ("active".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.and(
                    cb.isTrue(root.<Boolean>get("enabled")),
                    cb.isFalse(root.<Boolean>get("locked"))));
        } else if ("pending".equals(status)) {
            spec = spec.and((root, query, cb) -> cb.and(
                    cb.isFalse(root.<Boolean>get("enabled")),
                    cb.isFalse(root.<Boolean>get("locked"))));
        }

        return userRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    /** Các vai trò mà người thao tác được phép gán: Admin gán tất cả, Manager không gán Admin. */
    public List<Role> assignableRoles(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            return List.of(Role.values());
        }
        return Arrays.stream(Role.values()).filter(r -> r != Role.ADMIN).toList();
    }

    @Transactional
    public void lock(User actor, Long targetId) {
        User target = find(targetId);
        checkCanModify(actor, target);
        target.setLocked(true);
    }

    @Transactional
    public void unlock(User actor, Long targetId) {
        User target = find(targetId);
        checkCanModify(actor, target);
        target.setLocked(false);
    }

    @Transactional
    public void changeRole(User actor, Long targetId, Role newRole) {
        User target = find(targetId);
        checkCanModify(actor, target);
        if (!assignableRoles(actor).contains(newRole)) {
            throw new BusinessException("Bạn không có quyền gán vai trò này.");
        }
        target.setRole(newRole);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));
    }

    private void checkCanModify(User actor, User target) {
        if (actor.getId().equals(target.getId())) {
            throw new BusinessException("Bạn không thể thao tác trên chính tài khoản của mình.");
        }
        if (actor.getRole() == Role.MANAGER && target.getRole() == Role.ADMIN) {
            throw new BusinessException("Manager không có quyền thao tác trên tài khoản Admin.");
        }
    }
}