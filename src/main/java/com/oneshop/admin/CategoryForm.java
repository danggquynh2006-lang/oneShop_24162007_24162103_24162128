package com.oneshop.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {

    @NotBlank(message = "Vui lòng nhập tên danh mục")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    /** null nghĩa là danh mục gốc. */
    private Long parentId;

    private boolean active = true;
}