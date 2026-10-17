package com.oneshop.home;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.Category;

public interface HomeCategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c where c.parent is null and c.active = true order by c.name")
    List<Category> findActiveRoots();

    @Query("select c from Category c where c.parent.id = :parentId and c.active = true order by c.name")
    List<Category> findActiveChildren(@Param("parentId") Long parentId);
}