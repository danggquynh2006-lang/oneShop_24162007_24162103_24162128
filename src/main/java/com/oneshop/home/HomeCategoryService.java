package com.oneshop.home;

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
}