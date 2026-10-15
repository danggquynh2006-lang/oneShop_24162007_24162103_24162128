package com.oneshop.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.OtpType;
import com.oneshop.entity.OtpToken;
import com.oneshop.repository.OtpTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int EXPIRE_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_SECONDS = 60;

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom random = new SecureRandom();

    /** Tạo OTP mới, vô hiệu các OTP cũ chưa dùng. */
    @Transactional
    public OtpToken create(String email, OtpType type) {
        LocalDateTime now = LocalDateTime.now();
        List<OtpToken> olds = otpTokenRepository.findByEmailAndTypeAndUsedFalse(email, type);
        for (OtpToken old : olds) {
            LocalDateTime createdAt = old.getExpiredAt().minusMinutes(EXPIRE_MINUTES);
            long seconds = Duration.between(createdAt, now).getSeconds();
            if (seconds < RESEND_SECONDS) {
                throw new AuthException("Vui lòng đợi " + (RESEND_SECONDS - seconds) + " giây rồi gửi lại mã.");
            }
        }
        olds.forEach(t -> t.setUsed(true));

        String code = String.format("%06d", random.nextInt(1_000_000));
        return otpTokenRepository.save(OtpToken.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiredAt(now.plusMinutes(EXPIRE_MINUTES))
                .build());
    }

    /** Kiểm tra OTP, đúng thì đánh dấu đã dùng. Sai thì ném AuthException. */
    @Transactional(noRollbackFor = AuthException.class)
    public void verify(String email, OtpType type, String code) {
        OtpToken token = otpTokenRepository
                .findFirstByEmailAndTypeAndUsedFalseOrderByIdDesc(email, type)
                .orElseThrow(() -> new AuthException("Mã OTP không tồn tại hoặc đã được sử dụng. Vui lòng gửi lại mã."));

        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("Mã OTP đã hết hạn. Vui lòng gửi lại mã.");
        }
        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new AuthException("Bạn đã nhập sai quá nhiều lần. Vui lòng gửi lại mã mới.");
        }
        if (!token.getCode().equals(code)) {
            token.setAttempts(token.getAttempts() + 1);
            throw new AuthException("Mã OTP không đúng. Còn " + (MAX_ATTEMPTS - token.getAttempts()) + " lần thử.");
        }
        token.setUsed(true);
    }
}