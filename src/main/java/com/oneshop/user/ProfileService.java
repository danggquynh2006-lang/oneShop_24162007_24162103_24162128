package com.oneshop.user;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.oneshop.common.BusinessException;
import com.oneshop.config.CloudinaryService;
import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private static final long MAX_AVATAR_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public void updateProfile(Long userId, ProfileForm form, MultipartFile avatarFile) {
        User user = findUser(userId);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            validateAvatar(avatarFile);
            try {
                user.setAvatar(cloudinaryService.uploadImage(avatarFile, "avatars").url());
            } catch (IOException | RuntimeException e) {
                log.error("Upload avatar thất bại", e);
                throw new BusinessException("Tải ảnh lên thất bại, vui lòng thử lại.");
            }
        }
        user.setFullName(form.getFullName().trim());
        user.setPhone(form.getPhone().trim());
        userRepository.save(user);
    }

    public void changePassword(Long userId, ChangePasswordForm form) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
        }
        if (passwordEncoder.matches(form.getNewPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản."));
    }

    private void validateAvatar(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BusinessException("Ảnh đại diện tối đa 2MB.");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Ảnh đại diện chỉ chấp nhận JPG, PNG hoặc WEBP.");
        }
    }
}