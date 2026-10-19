package com.oneshop.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.oneshop.common.Role;
import com.oneshop.entity.Carrier;
import com.oneshop.entity.Category;
import com.oneshop.entity.User;
import com.oneshop.repository.CarrierRepository;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(0)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    // Mật khẩu chung cho các tài khoản mẫu, ghi vào README
    private static final String DEFAULT_PASSWORD = "Oneshop@123";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CarrierRepository carrierRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oneshop.seed-demo-users:false}")
    private boolean seedDemoUsers;

    @Override
    public void run(String... args) {
        if (seedDemoUsers) seedUsers();
        seedCategories();
        seedCarriers();
    }

    private void seedUsers() {
        createUser("admin@oneshop.com", "Quản trị viên", "0900000001", Role.ADMIN);
        createUser("manager@oneshop.com", "Quản lý sàn", "0900000002", Role.MANAGER);
        createUser("shipper@oneshop.com", "Nhân viên giao hàng", "0900000003", Role.SHIPPER);
        createUser("vendor@oneshop.com", "Chủ shop mẫu", "0900000004", Role.VENDOR);
        createUser("user@oneshop.com", "Khách hàng mẫu", "0900000005", Role.USER);
    }

    private void createUser(String email, String fullName, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .fullName(fullName)
                .phone(phone)
                .role(role)
                .enabled(true)
                .build());
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        addCategory("Chăm sóc da", "Sữa rửa mặt", "Toner", "Serum", "Kem dưỡng", "Kem chống nắng");
        addCategory("Trang điểm", "Son môi", "Kem nền", "Phấn mắt", "Mascara");
        addCategory("Chăm sóc tóc", "Dầu gội", "Dầu xả", "Dưỡng tóc");
        addCategory("Nước hoa");
    }

    private void addCategory(String parentName, String... childNames) {
        Category parent = categoryRepository.save(Category.builder().name(parentName).build());
        for (String childName : childNames) {
            categoryRepository.save(Category.builder().name(childName).parent(parent).build());
        }
    }

    private void seedCarriers() {
        if (carrierRepository.count() > 0) {
            return;
        }
        carrierRepository.saveAll(List.of(
                Carrier.builder().name("Giao Hàng Nhanh").shippingFee(new BigDecimal("30000")).build(),
                Carrier.builder().name("Giao Hàng Tiết Kiệm").shippingFee(new BigDecimal("22000")).build(),
                Carrier.builder().name("J&T Express").shippingFee(new BigDecimal("25000")).build()));
    }
}
