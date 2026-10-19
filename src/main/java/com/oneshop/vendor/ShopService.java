package com.oneshop.vendor;

import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.oneshop.common.BusinessException;
import com.oneshop.common.Role;
import com.oneshop.common.ShopStatus;
import com.oneshop.config.CloudinaryService;
import com.oneshop.entity.Shop;
import com.oneshop.entity.User;
import com.oneshop.repository.ShopRepository;
import com.oneshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class ShopService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Shop requireActiveShop(User user) {
        if (user == null) throw new BusinessException("Vui lòng đăng nhập.");
        Shop shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new BusinessException("Bạn chưa đăng ký shop."));
        if (shop.getStatus() != ShopStatus.ACTIVE) throw new BusinessException("Shop chưa được duyệt hoặc đã bị khóa.");
        return shop;
    }

    @Transactional(readOnly = true)
    public Shop findOwnShop(User user) {
        if (user == null) throw new BusinessException("Vui lòng đăng nhập.");
        return shopRepository.findByOwnerId(user.getId()).orElse(null);
    }

    @Transactional
    public Shop register(User user, ShopForm form, MultipartFile logo) throws IOException {
        if (user == null) throw new BusinessException("Vui lòng đăng nhập.");
        if (user.getRole() != Role.USER) throw new BusinessException("Chỉ tài khoản khách hàng được đăng ký shop.");
        if (shopRepository.findByOwnerId(user.getId()).isPresent()) throw new BusinessException("Tài khoản đã đăng ký shop.");
        Shop shop = Shop.builder().owner(userRepository.getReferenceById(user.getId()))
                .name(form.getName().trim()).description(form.getDescription())
                .address(form.getAddress().trim()).build();
        if (logo != null && !logo.isEmpty()) {
            validateImage(logo);
            shop.setLogo(cloudinaryService.uploadImage(logo, "shops").url());
        }
        return shopRepository.save(shop);
    }

    @Transactional
    public void update(User user, ShopForm form, MultipartFile logo, MultipartFile banner) throws IOException {
        Shop shop = requireActiveShop(user);
        shop.setName(form.getName().trim());
        shop.setDescription(form.getDescription());
        shop.setAddress(form.getAddress().trim());
        if (logo != null && !logo.isEmpty()) {
            validateImage(logo);
            shop.setLogo(cloudinaryService.uploadImage(logo, "shops").url());
        }
        if (banner != null && !banner.isEmpty()) {
            validateImage(banner);
            shop.setBanner(cloudinaryService.uploadImage(banner, "shops").url());
        }
    }

    @Transactional
    public void approve(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new BusinessException("Không tìm thấy shop."));
        if (shop.getStatus() != ShopStatus.PENDING) throw new BusinessException("Shop không ở trạng thái chờ duyệt.");
        shop.setStatus(ShopStatus.ACTIVE);
        User owner = shop.getOwner();
        owner.setRole(Role.VENDOR);
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024 || file.getContentType() == null
                || !file.getContentType().startsWith("image/"))
            throw new BusinessException("Chỉ nhận ảnh tối đa 10 MB.");
    }
}
