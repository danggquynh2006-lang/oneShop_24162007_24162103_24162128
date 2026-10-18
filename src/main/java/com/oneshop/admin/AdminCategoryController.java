package com.oneshop.admin;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.common.BusinessException;
import com.oneshop.entity.Category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String error,
                       Model model) {
        model.addAttribute("categories", categoryService.search(keyword, page));
        model.addAttribute("keyword", keyword);

        if ("created".equals(msg)) {
            model.addAttribute("message", "Đã thêm danh mục.");
        } else if ("updated".equals(msg)) {
            model.addAttribute("message", "Đã cập nhật danh mục.");
        } else if ("toggled".equals(msg)) {
            model.addAttribute("message", "Đã thay đổi trạng thái hiển thị.");
        } else if ("deleted".equals(msg)) {
            model.addAttribute("message", "Đã xóa danh mục.");
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/categories";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return showForm(model, new CategoryForm(), "Thêm danh mục", "/admin/categories", null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CategoryForm form, BindingResult result,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Model model) {
        if (!result.hasErrors()) {
            try {
                categoryService.create(form, imageFile);
                return "redirect:/admin/categories?msg=created";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Thêm danh mục", "/admin/categories", null);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.get(id);
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setParentId(category.getParent() == null ? null : category.getParent().getId());
        form.setActive(category.isActive());
        return showForm(model, form, "Sửa danh mục", "/admin/categories/" + id, category);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CategoryForm form,
                         BindingResult result,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         Model model) {
        if (!result.hasErrors()) {
            try {
                categoryService.update(id, form, imageFile);
                return "redirect:/admin/categories?msg=updated";
            } catch (BusinessException e) {
                model.addAttribute("formError", e.getMessage());
            }
        }
        return showForm(model, form, "Sửa danh mục", "/admin/categories/" + id, categoryService.get(id));
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        categoryService.toggleActive(id);
        return back(redirect, keyword, page, "toggled", null);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         RedirectAttributes redirect) {
        try {
            categoryService.delete(id);
            return back(redirect, keyword, page, "deleted", null);
        } catch (BusinessException e) {
            return back(redirect, keyword, page, null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return back(redirect, keyword, page, null,
                    "Danh mục đang được sử dụng (có sản phẩm) nên không thể xóa. Hãy bấm Ẩn thay vì xóa.");
        }
    }

    private String showForm(Model model, CategoryForm form, String title, String action, Category current) {
        Long currentId = current == null ? null : current.getId();
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("current", current);
        model.addAttribute("parents", categoryService.rootCategories(currentId));
        model.addAttribute("hasChildren", currentId != null && categoryService.hasChildren(currentId));
        return "admin/category-form";
    }

    private String back(RedirectAttributes redirect, String keyword, int page, String msg, String error) {
        redirect.addAttribute("keyword", keyword);
        redirect.addAttribute("page", page);
        if (msg != null) {
            redirect.addAttribute("msg", msg);
        }
        if (error != null) {
            redirect.addAttribute("error", error);
        }
        return "redirect:/admin/categories";
    }
}