package com.oneshop.ai;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeautyAssistantRequest(
        @NotBlank(message = "Bạn hãy nhập câu hỏi trước nhé.")
        @Size(max = 1000, message = "Câu hỏi tối đa 1.000 ký tự.")
        String message,
        @Size(max = 12, message = "Lịch sử trò chuyện quá dài.")
        List<ChatTurn> history) {

    public record ChatTurn(
            @NotBlank @Size(max = 1000) String role,
            @NotBlank @Size(max = 2000) String text) {
    }
}
