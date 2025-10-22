package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.service.*;
import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.dto.HoaDonTrackingDTO;
import com.example.datn_sd28_2025.dto.KhachHangDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private OpenAIService openAIService;
    
    @Autowired(required = false)
    private UltraChatService ultraChatService;

    @Autowired
    private ChatboxService chatboxService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private HoaDonService hoaDonService;
    
    @Autowired
    private ThongKeService thongKeService;
    
    @Autowired
    private KhachHangService khachHangService;
    
    @Autowired
    private NhanVienService nhanVienService;

    /**
     * API gửi tin nhắn đến chatbot
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String context = request.getOrDefault("context", "");

            if (message == null || message.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Tin nhắn không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Nhận tin nhắn từ khách hàng: {}", message);

            // Lấy context bổ sung
            String aiResponse = chatboxService.processChat(message, context);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            response.put("timestamp", System.currentTimeMillis());

            logger.info("Phản hồi từ AI: {}", aiResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý tin nhắn chatbot: ", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Fallback khi OpenAI lỗi/429/quota: sử dụng UltraChat nếu có, hoặc gợi ý sản phẩm theo từ khóa.
     */
    private String buildFallbackReply(String userMessage) { return chatboxService.buildFallbackReply(userMessage); }

    /**
     * API lấy thông tin sản phẩm để làm context
     */
    @GetMapping("/product-context")
    public ResponseEntity<Map<String, Object>> getProductContext() {
        try {
            String context = openAIService.getProductContext();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("context", context);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy context sản phẩm: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không thể lấy thông tin sản phẩm");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API kiểm tra trạng thái chatbot
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "online");
        response.put("message", "Chatbot đang hoạt động bình thường");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API tìm kiếm sản phẩm thông minh
     */
    @GetMapping("/search-products")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String query) {
        try {
            return ResponseEntity.ok(chatboxService.searchProductsResponse(query));
        } catch (Exception e) {
            logger.error("Error searching products", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tìm kiếm sản phẩm");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API tra cứu đơn hàng
     */
    @GetMapping("/check-order")
    public ResponseEntity<Map<String, Object>> checkOrder(@RequestParam String orderCode, @RequestParam String phone) {
        try {
            // Use getTrackingInfo method instead
            HoaDonTrackingDTO trackingInfo = hoaDonService.getTrackingInfo(orderCode);
            
            Map<String, Object> response = new HashMap<>();
            if (trackingInfo != null) {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("maHoaDon", trackingInfo.getMaHoaDon());
                orderInfo.put("tenKhachHang", trackingInfo.getTenKhachHang());
                orderInfo.put("soDienThoai", trackingInfo.getSoDienThoai());
                orderInfo.put("tenTrangThai", trackingInfo.getTenTrangThai());
                orderInfo.put("trangThai", trackingInfo.getTrangThai());
                orderInfo.put("tongTien", trackingInfo.getTongTien());
                
                response.put("success", true);
                response.put("order", orderInfo);
                response.put("found", true);
            } else {
                response.put("success", true);
                response.put("order", null);
                response.put("found", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking order", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tra cứu đơn hàng");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API lấy thống kê nhanh
     */
    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        try {
            // Get current date range for stats
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate startOfMonth = today.withDayOfMonth(1);
            
            Map<String, Object> stats = thongKeService.layThongKeChiTiet(startOfMonth, today);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting quick stats", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thống kê");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API so sánh sản phẩm (2–3 sản phẩm)
     * Trả về đầy đủ trường hiển thị để chatbox render bảng so sánh động.
     */
    @GetMapping("/compare-products")
    public ResponseEntity<Map<String, Object>> compareProducts(@RequestParam String productIds) {
        try {
            return ResponseEntity.ok(chatboxService.compareProductsResponse(productIds));
        } catch (Exception e) {
            logger.error("Error comparing products", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi so sánh sản phẩm");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    /**
     * Build verdict and quick reviews based on simple heuristics (price, RAM, pin, refresh rate)
     */
    private Map<String, Object> buildComparisonInsights(List<Map<String, Object>> products) {
        Map<String, Object> result = new HashMap<>();
        if (products == null || products.size() < 2) {
            result.put("verdict", "Cần ít nhất 2 sản phẩm để so sánh.");
            result.put("quickReviews", Map.of());
            return result;
        }

        // Extract normalized metrics per product
        class Metrics { String name; double price; int ram; int pin; int hz; }
        List<Metrics> list = new ArrayList<>();
        for (Map<String, Object> p : products) {
            Metrics m = new Metrics();
            m.name = str(p.getOrDefault("name", p.get("tenSanPham")));
            m.price = dbl(p.get("price"));
            if (Double.isNaN(m.price)) m.price = dbl(p.get("giaBan"));
            m.ram = parseRamGB(str(p.get("tenRam")));
            m.pin = parsePinMAh(str(p.get("tenPin")));
            m.hz = parseHz(str(p.get("tenManHinh")));
            if (m.name == null || m.name.isBlank()) m.name = "Sản phẩm";
            list.add(m);
        }

        // Score: higher RAM, PIN, HZ better; lower price better
        Map<String, Integer> score = new HashMap<>();
        for (Metrics a : list) score.put(a.name, 0);
        // Pairwise comparisons
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Metrics a = list.get(i), b = list.get(j);
                if (a.ram != b.ram) score.compute(a.ram > b.ram ? a.name : b.name, (k,v)->(v==null?0:v)+1);
                if (a.pin != b.pin) score.compute(a.pin > b.pin ? a.name : b.name, (k,v)->(v==null?0:v)+1);
                if (a.hz != b.hz) score.compute(a.hz > b.hz ? a.name : b.name, (k,v)->(v==null?0:v)+1);
                if (!Double.valueOf(a.price).equals(b.price)) score.compute(a.price < b.price ? a.name : b.name, (k,v)->(v==null?0:v)+1);
            }
        }

        // Pick best by score
        String best = null; int bestScore = Integer.MIN_VALUE;
        for (Map.Entry<String,Integer> e : score.entrySet()) {
            if (e.getValue() > bestScore) { bestScore = e.getValue(); best = e.getKey(); }
        }

        StringBuilder verdict = new StringBuilder();
        if (best != null) {
            verdict.append("Gợi ý: ").append(best).append(" có cân bằng thông số tốt hơn trong nhóm.");
        } else {
            verdict.append("Các sản phẩm có thông số tương đương, chọn theo thương hiệu/giá.");
        }

        // Build quick reviews per product
        Map<String, String> quick = new LinkedHashMap<>();
        for (Metrics m : list) {
            List<String> notes = new ArrayList<>();
            // Price band
            if (!Double.isNaN(m.price) && m.price > 0) {
                notes.add(m.price <= 10000000 ? "Giá tốt" : (m.price <= 20000000 ? "Giá tầm trung" : "Cao cấp"));
            }
            if (m.ram >= 12) notes.add("RAM rất cao");
            else if (m.ram >= 8) notes.add("RAM cao");
            if (m.pin >= 5000) notes.add("Pin trâu");
            if (m.hz >= 120) notes.add("Màn hình 120Hz");
            if (notes.isEmpty()) notes.add("Cấu hình cơ bản");
            quick.put(m.name, String.join(" • ", notes));
        }

        result.put("verdict", verdict.toString());
        result.put("quickReviews", quick);
        return result;
    }

    private String str(Object o) { return o == null ? null : String.valueOf(o); }
    private double dbl(Object o) {
        try { return o == null ? Double.NaN : Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return Double.NaN; }
    }
    private int parseRamGB(String s) {
        if (s == null) return 0; // e.g., "8GB", "12 GB"
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(s.replaceAll(",", ""));
            return m.find() ? Integer.parseInt(m.group(1)) : 0;
        } catch (Exception e) { return 0; }
    }
    private int parsePinMAh(String s) {
        if (s == null) return 0; // e.g., "5000 mAh"
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{3,5})").matcher(s.replaceAll(",", ""));
            return m.find() ? Integer.parseInt(m.group(1)) : 0;
        } catch (Exception e) { return 0; }
    }
    private int parseHz(String s) {
        if (s == null) return 0; // e.g., "AMOLED 120Hz"
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*hz", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s);
            return m.find() ? Integer.parseInt(m.group(1)) : 0;
        } catch (Exception e) { return 0; }
    }
    
    /**
     * API lấy thông tin khách hàng
     */
    @GetMapping("/customer-info")
    public ResponseEntity<Map<String, Object>> getCustomerInfo(@RequestParam String phone) {
        try {
            // Use findBySoDienThoai method
            Optional<KhachHangDTO> customerOpt = khachHangService.findBySoDienThoai(phone);
            
            Map<String, Object> response = new HashMap<>();
            if (customerOpt.isPresent()) {
                KhachHangDTO customerDTO = customerOpt.get();
                Map<String, Object> customer = new HashMap<>();
                customer.put("maKhachHang", customerDTO.getMaKhachHang());
                customer.put("hoTen", customerDTO.getHoTen());
                customer.put("soDienThoai", customerDTO.getSoDienThoai());
                customer.put("email", customerDTO.getEmail());
                customer.put("ngayTao", customerDTO.getNgayTao());
                
                response.put("success", true);
                response.put("customer", customer);
                response.put("found", true);
            } else {
                response.put("success", true);
                response.put("customer", null);
                response.put("found", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting customer info", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thông tin khách hàng");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API lấy danh sách sản phẩm nổi bật
     */
    @GetMapping("/featured-products")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts() {
        try {
            // Get all products and take first 6 as featured
            List<SanPhamDTO> productDTOs = sanPhamService.getAllSanPham();
            List<Map<String, Object>> products = productDTOs.stream()
                .limit(6)
                .map(dto -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", dto.getId());
                    productMap.put("tenSanPham", dto.getTenSanPham());
                    productMap.put("giaBan", dto.getGiaBan());
                    productMap.put("hinhAnh", dto.getHinhAnh());
                    productMap.put("soLuong", dto.getSoLuong());
                    productMap.put("tenHang", dto.getTenHang());
                    productMap.put("tenRam", dto.getTenRam());
                    productMap.put("tenRom", dto.getTenRom());
                    return productMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("products", products);
            response.put("count", products.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting featured products", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy sản phẩm nổi bật");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API lấy danh mục sản phẩm
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            // Get categories from SanPhamService - using a different approach
            List<Map<String, Object>> categories = new ArrayList<>();
            
            // Create sample categories for now
            Map<String, Object> category1 = new HashMap<>();
            category1.put("id", 1);
            category1.put("tenDanhMuc", "Điện thoại");
            categories.add(category1);
            
            Map<String, Object> category2 = new HashMap<>();
            category2.put("id", 2);
            category2.put("tenDanhMuc", "Phụ kiện");
            categories.add(category2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting categories", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh mục");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * API lấy thương hiệu
     */
    @GetMapping("/brands")
    public ResponseEntity<Map<String, Object>> getBrands() {
        try {
            // Get brands from SanPhamService - using a different approach
            List<Map<String, Object>> brands = new ArrayList<>();
            
            // Create sample brands for now
            Map<String, Object> brand1 = new HashMap<>();
            brand1.put("id", 1);
            brand1.put("tenHang", "Apple");
            brands.add(brand1);
            
            Map<String, Object> brand2 = new HashMap<>();
            brand2.put("id", 2);
            brand2.put("tenHang", "Samsung");
            brands.add(brand2);
            
            Map<String, Object> brand3 = new HashMap<>();
            brand3.put("id", 3);
            brand3.put("tenHang", "Xiaomi");
            brands.add(brand3);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("brands", brands);
            response.put("count", brands.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting brands", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy thương hiệu");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Xây dựng context đầy đủ
     */
    private String buildContext(String additionalContext) {
        StringBuilder context = new StringBuilder();
        
        // Thêm thông tin sản phẩm
        context.append("SẢN PHẨM HIỆN CÓ:\n");
        context.append(openAIService.getProductContext()).append("\n\n");
        
        // Thêm thông tin bổ sung nếu có
        if (additionalContext != null && !additionalContext.isEmpty()) {
            context.append("THÔNG TIN BỔ SUNG:\n");
            context.append(additionalContext).append("\n\n");
        }
        
        return context.toString();
    }
}
