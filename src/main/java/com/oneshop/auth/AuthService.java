package com.oneshop.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.oneshop.common.OtpType;
import com.oneshop.entity.OtpToken;
import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final MailService mailService;

    // ===== Đăng ký =====

    /** Đăng ký: lưu user (chưa kích hoạt) rồi gửi OTP. */
    public void register(RegisterRequest req) {
        String email = normalize(req.getEmail());
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.isEnabled()) {
            throw new AuthException("Email này đã được đăng ký.");
        }
        if (user == null) {
            user = User.builder().email(email).build();
        }
        // Nếu email đã đăng ký nhưng chưa kích hoạt: cho đăng ký lại, ghi đè thông tin
        user.setFullName(req.getFullName().trim());
        user.setPhone(req.getPhone().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        sendOtp(email, OtpType.REGISTER);
    }

    public void resendRegisterOtp(String email) {
        email = normalize(email);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEnabled()) {
            throw new AuthException("Không tìm thấy tài khoản cần kích hoạt.");
        }
        sendOtp(email, OtpType.REGISTER);
    }

    /** Nhập đúng OTP thì kích hoạt tài khoản. */
    public void verifyRegister(String email, String code) {
        email = normalize(email);
        otpService.verify(email, OtpType.REGISTER, code.trim());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Không tìm thấy tài khoản."));
        user.setEnabled(true);
        userRepository.save(user);
    }

    // ===== Đăng nhập =====

    /** Đăng nhập: kiểm tra email, mật khẩu, trạng thái tài khoản. */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(normalize(email)).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Email hoặc mật khẩu không đúng.");
        }
        if (!user.isEnabled()) {
            throw new AuthException("Tài khoản chưa được kích hoạt. Hãy đăng ký lại để nhận mã OTP mới.");
        }
        if (user.isLocked()) {
            throw new AuthException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        return user;
    }

    // ===== Quên mật khẩu =====

    /**
     * Gửi OTP đặt lại mật khẩu. Nếu email không tồn tại hoặc chưa kích hoạt thì
     * im lặng bỏ qua, để người ngoài không dò được email nào đã đăng ký.
     */
    public void requestPasswordReset(String email) {
        email = normalize(email);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isEnabled()) {
            return;
        }
        sendOtp(email, OtpType.FORGOT);
    }

    /** Nhập đúng OTP thì đổi sang mật khẩu mới (đã mã hóa BCrypt). */
    public void resetPassword(ResetPasswordRequest req) {
        String email = normalize(req.getEmail());
        otpService.verify(email, OtpType.FORGOT, req.getCode().trim());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Không tìm thấy tài khoản."));
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
    }

    // ===== Tiện ích =====

    private void sendOtp(String email, OtpType type) {
        OtpToken token = otpService.create(email, type);
        mailService.sendOtp(email, token.getCode(), type);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}