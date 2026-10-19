package com.oneshop.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.oneshop.common.OtpType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendOtp(String to, String code, OtpType type) {
        String purpose = type == OtpType.REGISTER ? "kích hoạt tài khoản" : "đặt lại mật khẩu";
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("[OneShop] Mã OTP " + purpose);
            message.setText("Mã OTP của bạn là: " + code
                    + "\nMã có hiệu lực trong 5 phút. Không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Gửi mail OTP tới {} thất bại", to, e);
        }
    }
}