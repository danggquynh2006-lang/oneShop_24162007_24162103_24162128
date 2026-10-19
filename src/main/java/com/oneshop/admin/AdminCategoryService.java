package com.oneshop.admin;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.config.CloudinaryService;
import com.oneshop.entity.Category;
import com.oneshop.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryService {

    private static final int PAGE_SIZE = 10;
    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<Category> search(String keyword, int page) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword == null ? "" : keyword.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
    }

    /** Các danh mục gốc dùng làm danh mục cha (loại trừ chính nó khi sửa). */
    @Transactional(readOnly = true)
    public List<Category> rootCategories(Long excludeId) {
        return categoryRepository.findByParentIsNullOrderByNameAsc().stream()
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasChildren(Long id) {
        return categoryRepository.existsByParentId(id);
    }

    @Transactional
    public Category create(CategoryForm form, MultipartFile image) {
        String name = form.getName().trim();
        Category parent = resolveParent(null, form.getParentId());
        checkDuplicateName(null, name, parent);

        Category category = Category.builder()
                .name(name)
                .parent(parent)
                .active(form.isActive())
                .build();
        if (image != null && !image.isEmpty()) {
            category.setImage(uploadImage(image));
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryForm form, MultipartFile image) {
        Category category = get(id);
        String name = form.getName().trim();
        Category parent = resolveParent(id, form.getParentId());
        checkDuplicateName(id, name, parent);

        category.setName(name);
        category.setParent(parent);
        category.setActive(form.isActive());
        if (image != null && !image.isEmpty()) {
            category.setImage(uploadImage(image));
        }
        return category;
    }

    /** Ẩn/hiện danh mục. Ẩn danh mục cha thì các danh mục con cũng bị ẩn. */
    @Transactional
    public void toggleActive(Long id) {
        Category category = get(id);
        boolean newActive = !category.isActive();
        category.setActive(newActive);
        if (!newActive && category.getParent() == null) {
            categoryRepository.findByParentId(id).forEach(child -> child.setActive(false));
        }
    }

    @Transactional
    public void delete(Long id) {
        Category category = get(id);
        if (categoryRepository.existsByParentId(id)) {
            throw new BusinessException(
                    "Danh mục đang có danh mục con, hãy xóa hoặc chuyển danh mục con trước.");
        }
        categoryRepository.delete(category);
        // flush để lỗi ràng buộc khóa ngoại (đang có sản phẩm) xuất hiện ngay tại đây
        categoryRepository.flush();
    }

    private Category resolveParent(Long selfId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(selfId)) {
            throw new BusinessException("Danh mục không thể là cha của chính nó.");
        }
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("Danh mục cha không tồn tại."));
        if (parent.getParent() != null) {
            throw new BusinessException("Chỉ hỗ trợ 2 cấp: danh mục cha phải là danh mục gốc.");
        }
        if (selfId != null && categoryRepository.existsByParentId(selfId)) {
            throw new BusinessException(
                    "Danh mục này đang có danh mục con nên không thể chuyển thành danh mục con.");
        }
        return parent;
    }

    private void checkDuplicateName(Long selfId, String name, Category parent) {
        Optional<Category> duplicate = parent == null
                ? categoryRepository.findFirstByNameIgnoreCaseAndParentIsNull(name)
                : categoryRepository.findFirstByNameIgnoreCaseAndParentId(name, parent.getId());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(selfId)) {
            throw new BusinessException("Tên danh mục đã tồn tại trong cùng cấp.");
        }
    }

    private String uploadImage(MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException("Ảnh danh mục tối đa 2MB.");
        }
        if (image.getContentType() == null || !ALLOWED_TYPES.contains(image.getContentType())) {
            throw new BusinessException("Ảnh chỉ chấp nhận JPG, PNG hoặc WEBP.");
        }
        try {
            return cloudinaryService.uploadImage(image, "categories").url();
        } catch (IOException | RuntimeException e) {
            log.error("Upload ảnh danh mục thất bại", e);
            throw new BusinessException("Tải ảnh lên thất bại, vui lòng thử lại.");
        }
    }
}