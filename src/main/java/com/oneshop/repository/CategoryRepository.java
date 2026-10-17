package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = "parent")
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<Category> findByParentIsNullOrderByNameAsc();

    List<Category> findByParentId(Long parentId);

    boolean existsByParentId(Long parentId);

    Optional<Category> findFirstByNameIgnoreCaseAndParentIsNull(String name);

    Optional<Category> findFirstByNameIgnoreCaseAndParentId(String name, Long parentId);
}