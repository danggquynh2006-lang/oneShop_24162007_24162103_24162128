package com.oneshop.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewForm {

    @Min(value = 1, message = "Vui lòng chọn số sao từ 1 đến 5.")
    @Max(value = 5, message = "Vui lòng chọn số sao từ 1 đến 5.")
    private int rating = 5;

    @NotBlank(message = "Vui lòng nhập nội dung đánh giá.")
    @Size(min = 50, max = 2000, message = "Bình luận phải từ 50 đến 2000 ký tự.")
    private String content;

    /** Ảnh / video đính kèm (tùy chọn, tối đa 5 tệp). */
    private List<MultipartFile> media = new ArrayList<>();
}