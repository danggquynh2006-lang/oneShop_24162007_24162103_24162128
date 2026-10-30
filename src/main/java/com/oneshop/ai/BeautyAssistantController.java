package com.oneshop.ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/beauty-assistant")
@RequiredArgsConstructor
@Validated
public class BeautyAssistantController {

    private final BeautyAssistantService assistant;

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        return Map.of("configured", assistant.isConfigured());
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@Valid @RequestBody BeautyAssistantRequest request) {
        return Map.of("reply", assistant.reply(request));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BeautyAssistantService.AssistantUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(
            BeautyAssistantService.AssistantUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", exception.getMessage()));
    }
}
