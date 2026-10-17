package com.oneshop.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrierForm {

    @NotBlank(message = "Vui lòng nhập tên nhà vận chuyển")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Vui lòng nhập phí vận chuyển")
    @DecimalMin(value = "0", message = "Phí vận chuyển không được âm")
    @Digits(integer = 9, fraction = 0,
            message = "Phí vận chuyển là số tiền VND, không có số lẻ và tối đa 9 chữ số")
    private BigDecimal shippingFee;

    private boolean active = true;
}