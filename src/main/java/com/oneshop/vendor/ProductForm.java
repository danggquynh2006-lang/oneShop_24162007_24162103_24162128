package com.oneshop.vendor;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductForm {
    @NotBlank @Size(max = 255)
    private String name;
    @Size(max = 4000)
    private String description;
    @NotNull @DecimalMin("0.01")
    private BigDecimal price;
    @NotNull @Min(0)
    private Integer stock;
    @NotNull @Min(0) @Max(90)
    private Integer discountPercent = 0;
    @NotNull
    private Long categoryId;
    @Size(max = 255)
    private String brand;
    private boolean active = true;
}
