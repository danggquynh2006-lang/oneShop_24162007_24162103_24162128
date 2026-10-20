package com.oneshop.vendor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.oneshop.common.PromotionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionForm {

    @NotBlank(message = "Vui lòng nhập mã giảm giá")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$",
             message = "Mã gồm 3-30 ký tự chữ, số, gạch ngang hoặc gạch dưới")
    private String code;

    @NotBlank(message = "Vui lòng nhập tên chương trình")
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại khuyến mãi")
    private PromotionType type;

    // PERCENT_PRODUCT: số %; loại còn lại: số tiền VND
    @NotNull(message = "Vui lòng nhập giá trị giảm")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal value;

    @NotNull(message = "Vui lòng nhập đơn tối thiểu (0 nếu không yêu cầu)")
    @DecimalMin(value = "0", message = "Đơn tối thiểu không được âm")
    private BigDecimal minOrder = BigDecimal.ZERO;

    // Chỉ dùng cho loại %, để trống = không giới hạn
    @DecimalMin(value = "0", message = "Giảm tối đa không được âm")
    private BigDecimal maxDiscount;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;

    @NotNull(message = "Vui lòng nhập số lượng")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity = 100;

    private boolean active = true;
}