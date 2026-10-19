package com.oneshop.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ShopForm {
    @NotBlank @Size(max = 255)
    private String name;
    @Size(max = 2000)
    private String description;
    @NotBlank @Size(max = 255)
    private String address;
}
