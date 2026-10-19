package com.oneshop.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressForm {

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 số, bắt đầu bằng 0")
    private String phone;

    @NotBlank(message = "Vui lòng nhập tỉnh/thành phố")
    private String province;

    @NotBlank(message = "Vui lòng nhập phường/xã")
    private String ward;

    @NotBlank(message = "Vui lòng nhập địa chỉ cụ thể")
    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự")
    private String detail;

    private boolean defaultAddress;
}