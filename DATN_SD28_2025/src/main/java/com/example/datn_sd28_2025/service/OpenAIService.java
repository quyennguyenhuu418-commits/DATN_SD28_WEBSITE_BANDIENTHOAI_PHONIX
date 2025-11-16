package com.example.datn_sd28_2025.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    @Value("${openai.enabled:true}")
    private boolean enabled;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SanPhamService sanPhamService;

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Gửi tin nhắn đến OpenAI và nhận phản hồi
     */
    public String sendMessage(String userMessage, String context) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return "Xin lỗi, chatbot chưa được cấu hình API key. Vui lòng liên hệ quản trị viên.";
            }

            // Tạo system prompt với thông tin về hệ thống
            String systemPrompt = createSystemPrompt(context);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            List<Map<String, String>> messages = new ArrayList<>();
            
            // System message
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            // User message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Gửi request
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }

            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.";

        } catch (HttpClientErrorException e) {
            logger.error("Lỗi khi gọi OpenAI API: ", e);
            // Propagate quota/rate-limit to allow controller fallback
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                (e.getResponseBodyAsString() != null && e.getResponseBodyAsString().contains("insufficient_quota"))) {
                throw new RuntimeException("OPENAI_QUOTA_OR_RATE_LIMIT", e);
            }
            return "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
        } catch (Exception e) {
            logger.error("Lỗi khi gọi OpenAI API: ", e);
            return "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.";
        }
    }

    /**
     * Tạo system prompt với thông tin về hệ thống
     */
    private String createSystemPrompt(String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một trợ lý AI thông minh của cửa hàng điện thoại PhoniX Store. ");
        prompt.append("Bạn có thể trả lời mọi câu hỏi về sản phẩm, dịch vụ, chính sách và hệ thống của cửa hàng.\n\n");
        
        prompt.append("THÔNG TIN VỀ HỆ THỐNG:\n");
        prompt.append("- Cửa hàng: PhoniX Store - Chuyên bán điện thoại, tablet, laptop và phụ kiện\n");
        prompt.append("- Địa chỉ: Hà Nội, Việt Nam\n");
        prompt.append("- Website: Hệ thống quản lý bán hàng tích hợp\n\n");
        
        prompt.append("SẢN PHẨM CHÍNH:\n");
        prompt.append("- Điện thoại: iPhone, Samsung, Xiaomi, Vivo, OPPO\n");
        prompt.append("- Tablet, Laptop, Phụ kiện, Đồng hồ thông minh\n");
        prompt.append("- Các sản phẩm có đầy đủ thông tin: RAM, ROM, Camera, Pin, Chip, Màn hình\n\n");
        
        prompt.append("DỊCH VỤ:\n");
        prompt.append("- Bán hàng trực tiếp tại cửa hàng\n");
        prompt.append("- Bán hàng online với giao hàng tận nơi\n");
        prompt.append("- Hỗ trợ khách hàng 24/7\n");
        prompt.append("- Bảo hành chính hãng\n");
        prompt.append("- Tư vấn kỹ thuật\n\n");
        
        prompt.append("CHÍNH SÁCH:\n");
        prompt.append("- Hỗ trợ thanh toán: Tiền mặt, Chuyển khoản, Thẻ tín dụng, Ví điện tử\n");
        prompt.append("- Chương trình khuyến mãi thường xuyên\n");
        prompt.append("- Giảm giá cho khách hàng thân thiết\n");
        prompt.append("- Đổi trả trong 7 ngày\n\n");
        
        prompt.append("HỆ THỐNG QUẢN LÝ:\n");
        prompt.append("- Quản lý sản phẩm: Danh mục, Hãng, Chi tiết kỹ thuật\n");
        prompt.append("- Quản lý khách hàng: Thông tin, Lịch sử mua hàng\n");
        prompt.append("- Quản lý nhân viên: Phân ca, Giao ca, Hiệu suất\n");
        prompt.append("- Quản lý đơn hàng: Tạo, Xử lý, Theo dõi trạng thái\n");
        prompt.append("- Quản lý kho: IMEI, Số lượng, Nhập xuất\n");
        prompt.append("- Thống kê báo cáo: Doanh thu, Sản phẩm bán chạy, Khách hàng\n");
        prompt.append("- Hệ thống chat hỗ trợ: Kết nối khách hàng với nhân viên\n\n");
        
        prompt.append("QUY TẮC TRẢ LỜI:\n");
        prompt.append("1. Luôn trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp\n");
        prompt.append("2. Cung cấp thông tin chính xác về sản phẩm và dịch vụ\n");
        prompt.append("3. Nếu không biết thông tin cụ thể, hãy hướng dẫn khách hàng liên hệ trực tiếp\n");
        prompt.append("4. Luôn sẵn sàng hỗ trợ và tư vấn\n");
        prompt.append("5. Sử dụng emoji phù hợp để tạo cảm giác thân thiện\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("THÔNG TIN BỔ SUNG:\n");
            prompt.append(context);
        }
        
        return prompt.toString();
    }

    /**
     * Lấy thông tin sản phẩm để làm context
     */
    public String getProductContext() {
        try {
            if (sanPhamService == null) {
                return "Sản phẩm hiện có: iPhone, Samsung Galaxy, Xiaomi, Vivo, OPPO";
            }
            List<com.example.datn_sd28_2025.dto.SanPhamDTO> products = sanPhamService.getActiveSanPham();
            if (products == null || products.isEmpty()) products = sanPhamService.getAllSanPham();
            StringBuilder sb = new StringBuilder();
            sb.append("Danh mục sản phẩm (rút gọn)\n");
            int limit = Math.min(30, products != null ? products.size() : 0);
            for (int i = 0; i < limit; i++) {
                var p = products.get(i);
                sb.append("- ")
                  .append(Optional.ofNullable(p.getTenSanPham()).orElse("Sản phẩm"))
                  .append(" | Giá: ")
                  .append(p.getGiaBan() != null ? String.format("%,.0f VND", p.getGiaBan()) : "Liên hệ")
                  .append(" | Hãng: ")
                  .append(Optional.ofNullable(p.getTenHang()).orElse("—"))
                  .append(" | RAM: ")
                  .append(Optional.ofNullable(p.getTenRam()).orElse("—"))
                  .append(" | ROM: ")
                  .append(Optional.ofNullable(p.getTenRom()).orElse("—"))
                  .append(" | Chip: ")
                  .append(Optional.ofNullable(p.getTenChip()).orElse("—"))
                  .append(" | Pin: ")
                  .append(Optional.ofNullable(p.getTenPin()).orElse("—"))
                  .append("\n");
            }
            if (limit == 0) {
                return "Sản phẩm hiện có: iPhone, Samsung Galaxy, Xiaomi, Vivo, OPPO";
            }
            return sb.toString();
        } catch (Exception e) {
            return "Sản phẩm hiện có: iPhone, Samsung Galaxy, Xiaomi, Vivo, OPPO";
        }
    }

    /**
     * Lấy thông tin khuyến mãi để làm context
     */
    public String getPromotionContext() {
        // Có thể mở rộng để lấy thông tin khuyến mãi từ database
        return "Khuyến mãi hiện tại: Giảm giá 10% cho đơn hàng trên 2 triệu, Giảm 200K cho đơn hàng trên 1 triệu";
    }
}
