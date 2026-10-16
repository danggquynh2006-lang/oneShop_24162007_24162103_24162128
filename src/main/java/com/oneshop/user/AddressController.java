package com.oneshop.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.oneshop.auth.SecurityUtil;
import com.oneshop.entity.Address;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public String list(@RequestParam(required = false) String msg, Model model) {
        model.addAttribute("addresses", addressService.list(userId()));
        if ("saved".equals(msg)) {
            model.addAttribute("message", "Đã lưu địa chỉ.");
        } else if ("deleted".equals(msg)) {
            model.addAttribute("message", "Đã xóa địa chỉ.");
        } else if ("default".equals(msg)) {
            model.addAttribute("message", "Đã đặt làm địa chỉ mặc định.");
        }
        return "user/addresses";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return showForm(model, new AddressForm(), "Thêm địa chỉ mới", "/user/addresses");
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AddressForm form,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            return showForm(model, form, "Thêm địa chỉ mới", "/user/addresses");
        }
        addressService.create(userId(), form);
        return "redirect:/user/addresses?msg=saved";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Address a = addressService.get(userId(), id);
        AddressForm form = new AddressForm();
        form.setReceiverName(a.getReceiverName());
        form.setPhone(a.getPhone());
        form.setProvince(a.getProvince());
        form.setWard(a.getWard());
        form.setDetail(a.getDetail());
        form.setDefaultAddress(a.isDefaultAddress());
        return showForm(model, form, "Sửa địa chỉ", "/user/addresses/" + id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") AddressForm form,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            return showForm(model, form, "Sửa địa chỉ", "/user/addresses/" + id);
        }
        addressService.update(userId(), id, form);
        return "redirect:/user/addresses?msg=saved";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        addressService.delete(userId(), id);
        return "redirect:/user/addresses?msg=deleted";
    }

    @PostMapping("/{id}/default")
    public String setDefault(@PathVariable Long id) {
        addressService.setDefault(userId(), id);
        return "redirect:/user/addresses?msg=default";
    }

    private String showForm(Model model, AddressForm form, String title, String action) {
        model.addAttribute("form", form);
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        return "user/address-form";
    }

    private Long userId() {
        return SecurityUtil.getCurrentUser().getId();
    }
}