package com.oneshop.home;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.entity.Category;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeCategoryService {

    public record Node(Long id, String name, List<Item> children) {}
    public record Item(Long id, String name) {}

    private final HomeCategoryRepository repo;

    public List<Node> activeTree() {
        return repo.findActiveRoots().stream()
                .map(root -> new Node(
                        root.getId(),
                        root.getName(),
                        repo.findActiveChildren(root.getId()).stream()
                                .map(c -> new Item(c.getId(), c.getName()))
                                .toList()))
                .toList();
    }
    
    public record CategoryView(Long id, String name, Long parentId, String parentName, List<Item> children) {}

    @Transactional(readOnly = true)
    public Optional<CategoryView> findVisible(Long id) {
        return repo.findById(id).filter(Category::isActive).flatMap(c -> {
            Category parent = c.getParent();
            if (parent != null && !parent.isActive()) {
                return Optional.empty();
            }
            List<Item> children = parent == null
                    ? repo.findActiveChildren(c.getId()).stream()
                            .map(x -> new Item(x.getId(), x.getName())).toList()
                    : List.of();
            return Optional.of(new CategoryView(
                    c.getId(), c.getName(),
                    parent == null ? null : parent.getId(),
                    parent == null ? null : parent.getName(),
                    children));
        });
    }
}

