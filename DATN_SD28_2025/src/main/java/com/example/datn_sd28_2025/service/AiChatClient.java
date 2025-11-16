package com.example.datn_sd28_2025.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Minimal OpenAI Chat Completions client using Java 11 HttpClient.
 */
@Service
public class AiChatClient {

    @Value("${chat.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${chat.ai.baseUrl:https://api.openai.com}")
    private String baseUrl;

    @Value("${chat.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${chat.ai.apiKey:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private volatile String lastModelUsed = null;

    public String getLastModelUsed() { return lastModelUsed; }

    public boolean isEnabled() {
        return aiEnabled && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generate a reply from OpenAI given user text and optional context map.
     */
    public String generateReply(String userText, Map<String, Object> context) throws Exception {
        if (!isEnabled()) {
            throw new IllegalStateException("AI is disabled or API key missing");
        }

        String url = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";

        // Try configured model first, then fallbacks
        String[] modelsToTry = new String[] { model, "gpt-3.5-turbo", "gpt-4o-mini" };
        Exception lastError = null;
        for (String m : modelsToTry) {
            if (m == null || m.isBlank()) continue;

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", m);
            body.put("temperature", 0.5);
            body.put("max_tokens", 300);

            ArrayNode messages = body.putArray("messages");

        String systemPrompt = "Bạn là trợ lý bán hàng điện thoại của PhoniX. Hãy trả lời ngắn gọn, hữu ích, ưu tiên tiếng Việt. " +
                "Nếu người dùng muốn tư vấn, hãy hỏi rõ ngân sách, thương hiệu, nhu cầu (gaming, camera, pin).";
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        if (context != null && !context.isEmpty()) {
            ObjectNode ctx = objectMapper.createObjectNode();
            ctx.put("role", "system");
            ctx.put("content", "Ngữ cảnh phiên: " + objectMapper.writeValueAsString(context));
            messages.add(ctx);
        }

            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", userText);
            messages.add(user);

            String payload = body.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode choices = root.path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode msg = choices.get(0).path("message").path("content");
                        if (msg.isTextual()) {
                            lastModelUsed = m;
                            return msg.asText();
                        }
                    }
                    return "Xin lỗi, tôi chưa có câu trả lời phù hợp.";
                } else {
                    // Try next model on typical model errors
                    lastError = new RuntimeException("OpenAI error(" + m + "): " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                lastError = e;
                // continue to next model
            }
        }
        if (lastError != null) throw lastError;
        return null;
    }
}


