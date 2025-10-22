package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.service.SanPhamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Centralized chatbox logic: AI fallback, product search and comparison helpers.
 * This service consolidates scattered chatbox-related logic so controllers stay thin.
 */
@Service
public class ChatboxService {

    private static final Logger logger = LoggerFactory.getLogger(ChatboxService.class);

    @Autowired(required = false)
    private OpenAIService openAIService;

    @Autowired(required = false)
    private UltraChatService ultraChatService;

    @Autowired
    private SanPhamService sanPhamService;

    /**
     * Full chat processing with graceful fallback and optional LLM.
     */
    public String processChat(String message, String additionalContext) {
        try {
            String fullContext = buildContext(additionalContext);

            // Try OpenAI first if configured
            if (openAIService != null && openAIService.isEnabled()) {
                try {
                    String aiResponse = openAIService.sendMessage(message, fullContext);
                    if (aiResponse == null || aiResponse.isBlank() ||
                        aiResponse.startsWith("Xin lỗi") ||
                        aiResponse.toLowerCase().contains("api key") ||
                        aiResponse.toLowerCase().contains("chưa được cấu hình")) {
                        return buildFallbackReply(message);
                    }
                    return aiResponse;
                } catch (Exception ex) {
                    logger.warn("OpenAI error, using fallback.", ex);
                    return buildFallbackReply(message);
                }
            }
            // No OpenAI → fallback
            return buildFallbackReply(message);
        } catch (Exception e) {
            logger.error("processChat error", e);
            return "Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn.";
        }
    }

    /**
     * Enhanced fallback reply with intelligent context-aware logic.
     */
    public String buildFallbackReply(String userMessage) {
        try {
            // Analyze user intent and context
            IntentAnalysis intent = analyzeUserIntent(userMessage);
            
            // Try UltraChat with enhanced context
            if (ultraChatService != null) {
                Map<String, Object> intentMap = Map.of(
                    "type", intent.getType(),
                    "confidence", intent.getConfidence(),
                    "entities", intent.getEntities(),
                    "context", intent.getContext()
                );
                Map<String, Object> reply = ultraChatService.generateIntelligentResponse(userMessage, intentMap, "chatbox", "fallback");
                if (reply != null && "text".equals(reply.get("type"))) {
                    Object content = reply.get("content");
                    if (content != null) return String.valueOf(content);
                }
            }
            
            // Generate smart fallback based on intent
            return generateSmartFallbackReply(userMessage, intent);
            
        } catch (Exception e) {
            logger.warn("Error in buildFallbackReply: ", e);
            return "Hiện chatbot AI đang bận hoặc vượt hạn mức. Bạn có thể thử: \n" +
                   "• Tìm sản phẩm: ví dụ 'iPhone 15 256GB'\n" +
                   "• So sánh: 'so sánh iPhone 15 Pro và Galaxy S24'\n" +
                   "• Tra cứu đơn: 'mã đơn PX1234 số 09xxxxxxxx'";
        }
    }

    /**
     * Enhanced search products for chatbox UI with advanced filtering.
     */
    public Map<String, Object> searchProductsResponse(String query) {
        // Parse query for specific criteria
        SearchCriteria criteria = parseSearchCriteria(query);
        
        List<SanPhamDTO> productDTOs;
        // Use regular search for now, filter results based on criteria
        productDTOs = sanPhamService.searchSanPham(query);
        
        // Apply additional filtering based on criteria
        if (criteria.hasSpecificCriteria()) {
            productDTOs = filterProductsByCriteria(productDTOs, criteria);
        }
        
        List<Map<String, Object>> products = new ArrayList<>();
        for (SanPhamDTO dto : productDTOs) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", dto.getId());
            m.put("tenSanPham", dto.getTenSanPham());
            m.put("giaBan", dto.getGiaBan());
            m.put("hinhAnh", dto.getHinhAnh());
            m.put("soLuong", dto.getSoLuong());
            m.put("tenHang", dto.getTenHang());
            m.put("tenHeDieuHanh", dto.getTenHeDieuHanh());
            m.put("tenChip", dto.getTenChip());
            m.put("tenCpu", dto.getTenCpu());
            m.put("tenGpu", dto.getTenGpu());
            m.put("tenPin", dto.getTenPin());
            m.put("tenCameraSau", dto.getTenCameraSau());
            m.put("tenCameraTruoc", dto.getTenCameraTruoc());
            m.put("tenSim", dto.getTenSim());
            m.put("tenManHinh", dto.getTenManHinh());
            
            // Heuristic: infer RAM/ROM from name if null
            String ram = dto.getTenRam();
            String rom = dto.getTenRom();
            if (ram == null || ram.isBlank()) ram = inferRamFromName(dto.getTenSanPham());
            if (rom == null || rom.isBlank()) rom = inferRomFromName(dto.getTenSanPham());
            m.put("tenRam", ram);
            m.put("tenRom", rom);
            
            // Add search relevance score
            m.put("relevanceScore", calculateRelevanceScore(dto, criteria));
            
            products.add(m);
        }
        
        // Sort by relevance score
        products.sort((a, b) -> {
            Double scoreA = (Double) a.get("relevanceScore");
            Double scoreB = (Double) b.get("relevanceScore");
            return Double.compare(scoreB, scoreA);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("products", products);
        response.put("count", products.size());
        response.put("query", query);
        response.put("criteria", criteria.toMap());
        return response;
    }

    /**
     * Enhanced compare up to three products with detailed analysis and recommendations.
     */
    public Map<String, Object> compareProductsResponse(String productIdsCsv) {
        String[] ids = productIdsCsv.split(",");
        List<Map<String, Object>> products = new ArrayList<>();

        for (String id : ids) {
            String trimmed = id == null ? null : id.trim();
            if (trimmed == null || trimmed.isEmpty()) continue;
            try {
                Integer pid = Integer.parseInt(trimmed);
                Optional<SanPhamDTO> opt = sanPhamService.getSanPhamById(pid);
                if (opt.isPresent()) {
                    SanPhamDTO dto = opt.get();
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", dto.getId());
                    p.put("name", dto.getTenSanPham());
                    p.put("tenSanPham", dto.getTenSanPham());
                    p.put("price", dto.getGiaBan());
                    p.put("giaBan", dto.getGiaBan());
                    p.put("giaThamKhao", dto.getGiaBan());
                    p.put("imageUrl", dto.getHinhAnh());
                    p.put("hinhAnh", dto.getHinhAnh());
                    p.put("stock", dto.getSoLuong());
                    p.put("tenHang", ns(dto.getTenHang()));
                    
                    // Enhanced specs with inference
                    String ram = dto.getTenRam();
                    String rom = dto.getTenRom();
                    if (ram == null || ram.isBlank()) ram = inferRamFromName(dto.getTenSanPham());
                    if (rom == null || rom.isBlank()) rom = inferRomFromName(dto.getTenSanPham());
                    p.put("tenRam", ns(ram));
                    p.put("tenRom", ns(rom));
                    p.put("tenHeDieuHanh", ns(dto.getTenHeDieuHanh()));
                    p.put("tenManHinh", ns(dto.getTenManHinh()));
                    p.put("tenChip", ns(dto.getTenChip()));
                    p.put("tenPin", ns(dto.getTenPin()));
                    p.put("tenCpu", ns(dto.getTenCpu()));
                    p.put("tenGpu", ns(dto.getTenGpu()));
                    p.put("tenCameraTruoc", ns(dto.getTenCameraTruoc()));
                    p.put("tenCameraSau", ns(dto.getTenCameraSau()));
                    p.put("tenSim", ns(dto.getTenSim()));
                    
                    // Add calculated specs for comparison
                    p.put("ramGB", ramGB(ram));
                    p.put("romGB", ramGB(rom)); // Reuse ramGB for ROM parsing
                    p.put("pinMAh", pinMAh(ns(dto.getTenPin())));
                    p.put("screenHz", hz(ns(dto.getTenManHinh())));
                    p.put("priceValue", dto.getGiaBan() != null ? dto.getGiaBan().doubleValue() : 0.0);
                    
                    // Add performance score
                    p.put("performanceScore", calculatePerformanceScore(dto));
                    
                    products.add(p);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid product ID: {}", id);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("products", products);
        response.put("count", products.size());
        response.putAll(buildEnhancedComparisonInsights(products));
        return response;
    }

    private String buildContext(String additionalContext) {
        StringBuilder context = new StringBuilder();
        try {
            if (openAIService != null) {
                context.append("SẢN PHẨM HIỆN CÓ:\n");
                context.append(openAIService.getProductContext()).append("\n\n");
            }
        } catch (Exception ignore) {}
        if (additionalContext != null && !additionalContext.isEmpty()) {
            context.append("THÔNG TIN BỔ SUNG:\n");
            context.append(additionalContext).append("\n\n");
        }
        return context.toString();
    }

    private String ns(String s) { return s == null ? "" : s; }

    private Map<String, Object> buildEnhancedComparisonInsights(List<Map<String, Object>> products) {
        Map<String, Object> result = new HashMap<>();
        if (products == null || products.size() < 2) {
            result.put("verdict", "Cần ít nhất 2 sản phẩm để so sánh.");
            result.put("quickReviews", Map.of());
            result.put("recommendations", List.of());
            return result;
        }

        // Enhanced metrics with more details
        List<EnhancedMetrics> list = new ArrayList<>();
        for (Map<String, Object> p : products) {
            EnhancedMetrics m = new EnhancedMetrics();
            m.name = s(p.getOrDefault("name", p.get("tenSanPham")));
            m.price = d(p.get("price"));
            if (Double.isNaN(m.price)) m.price = d(p.get("giaBan"));
            m.ram = (Integer) p.getOrDefault("ramGB", 0);
            m.rom = (Integer) p.getOrDefault("romGB", 0);
            m.pin = (Integer) p.getOrDefault("pinMAh", 0);
            m.hz = (Integer) p.getOrDefault("screenHz", 0);
            m.performanceScore = (Double) p.getOrDefault("performanceScore", 0.0);
            m.brand = s(p.get("tenHang"));
            m.os = s(p.get("tenHeDieuHanh"));
            m.chip = s(p.get("tenChip"));
            if (m.name == null || m.name.isBlank()) m.name = "Sản phẩm";
            list.add(m);
        }

        // Calculate comprehensive scores
        Map<String, Double> scores = new HashMap<>();
        for (EnhancedMetrics m : list) {
            double score = 0.0;
            
            // Performance score (40%)
            score += m.performanceScore * 0.4;
            
            // Value for money (30%)
            if (m.price > 0) {
                double valueScore = (m.ram + m.rom/10 + m.pin/1000 + m.hz/10) / (m.price / 1000000);
                score += Math.min(valueScore * 10, 30.0);
            }
            
            // Brand reputation (15%)
            if (m.brand != null) {
                if (m.brand.toLowerCase().contains("apple")) score += 15.0;
                else if (m.brand.toLowerCase().contains("samsung")) score += 12.0;
                else if (m.brand.toLowerCase().contains("xiaomi")) score += 8.0;
                else score += 5.0;
            }
            
            // Modern features (15%)
            if (m.hz >= 120) score += 5.0;
            if (m.ram >= 8) score += 5.0;
            if (m.rom >= 128) score += 3.0;
            if (m.pin >= 4000) score += 2.0;
            
            scores.put(m.name, score);
        }

        // Find best product
        String best = null; double bestScore = Double.MIN_VALUE;
        for (Map.Entry<String, Double> e : scores.entrySet()) {
            if (e.getValue() > bestScore) { 
                bestScore = e.getValue(); 
                best = e.getKey(); 
            }
        }

        // Generate detailed verdict
        StringBuilder verdict = new StringBuilder();
        if (best != null) {
            verdict.append("🏆 **Gợi ý tốt nhất:** ").append(best).append("\n\n");
            
            // Add specific recommendations
            List<String> recommendations = generateRecommendations(list, best);
            if (!recommendations.isEmpty()) {
                verdict.append("💡 **Lý do:**\n");
                for (String rec : recommendations) {
                    verdict.append("• ").append(rec).append("\n");
                }
            }
        } else {
            verdict.append("Các sản phẩm có thông số tương đương, chọn theo thương hiệu/giá.");
        }

        // Enhanced quick reviews
        Map<String, String> quick = new LinkedHashMap<>();
        for (EnhancedMetrics m : list) {
            List<String> notes = new ArrayList<>();
            
            // Price category
            if (!Double.isNaN(m.price) && m.price > 0) {
                if (m.price <= 5000000) notes.add("💰 Giá rẻ");
                else if (m.price <= 10000000) notes.add("💰 Giá tốt");
                else if (m.price <= 20000000) notes.add("💰 Giá tầm trung");
                else notes.add("💰 Cao cấp");
            }
            
            // Performance
            if (m.ram >= 12) notes.add("⚡ RAM rất cao");
            else if (m.ram >= 8) notes.add("⚡ RAM cao");
            else if (m.ram >= 6) notes.add("⚡ RAM vừa");
            
            if (m.rom >= 256) notes.add("💾 Bộ nhớ lớn");
            else if (m.rom >= 128) notes.add("💾 Bộ nhớ vừa");
            
            if (m.pin >= 5000) notes.add("🔋 Pin trâu");
            else if (m.pin >= 4000) notes.add("🔋 Pin tốt");
            
            if (m.hz >= 120) notes.add("📱 Màn hình 120Hz");
            else if (m.hz >= 90) notes.add("📱 Màn hình 90Hz");
            
            // Brand specific
            if (m.brand != null) {
                if (m.brand.toLowerCase().contains("apple")) notes.add("🍎 iOS");
                else if (m.brand.toLowerCase().contains("samsung")) notes.add("📱 One UI");
                else if (m.brand.toLowerCase().contains("xiaomi")) notes.add("📱 MIUI");
            }
            
            if (notes.isEmpty()) notes.add("📱 Cấu hình cơ bản");
            quick.put(m.name, String.join(" • ", notes));
        }

        result.put("verdict", verdict.toString());
        result.put("quickReviews", quick);
        result.put("scores", scores);
        result.put("recommendations", generateDetailedRecommendations(list));
        return result;
    }
    
    private double calculatePerformanceScore(SanPhamDTO product) {
        double score = 0.0;
        
        // RAM score (0-25)
        int ram = ramGB(product.getTenRam());
        if (ram >= 12) score += 25.0;
        else if (ram >= 8) score += 20.0;
        else if (ram >= 6) score += 15.0;
        else if (ram >= 4) score += 10.0;
        else score += 5.0;
        
        // ROM score (0-20)
        int rom = ramGB(product.getTenRom());
        if (rom >= 256) score += 20.0;
        else if (rom >= 128) score += 15.0;
        else if (rom >= 64) score += 10.0;
        else score += 5.0;
        
        // Battery score (0-20)
        int battery = pinMAh(product.getTenPin());
        if (battery >= 5000) score += 20.0;
        else if (battery >= 4000) score += 15.0;
        else if (battery >= 3000) score += 10.0;
        else score += 5.0;
        
        // Screen refresh rate (0-15)
        int hz = hz(product.getTenManHinh());
        if (hz >= 120) score += 15.0;
        else if (hz >= 90) score += 10.0;
        else if (hz >= 60) score += 5.0;
        
        // Chip performance (0-20)
        String chip = product.getTenChip();
        if (chip != null) {
            String chipLower = chip.toLowerCase();
            if (chipLower.contains("a17") || chipLower.contains("a16")) score += 20.0;
            else if (chipLower.contains("a15") || chipLower.contains("a14")) score += 18.0;
            else if (chipLower.contains("snapdragon 8 gen") || chipLower.contains("exynos 2200")) score += 16.0;
            else if (chipLower.contains("snapdragon 7") || chipLower.contains("exynos 2100")) score += 12.0;
            else if (chipLower.contains("snapdragon 6") || chipLower.contains("dimensity")) score += 8.0;
            else score += 5.0;
        }
        
        return Math.min(score, 100.0);
    }
    
    private List<String> generateRecommendations(List<EnhancedMetrics> products, String bestProduct) {
        List<String> recommendations = new ArrayList<>();
        
        for (EnhancedMetrics product : products) {
            if (product.name.equals(bestProduct)) {
                if (product.ram >= 8) recommendations.add("RAM " + product.ram + "GB đảm bảo hiệu năng mượt mà");
                if (product.rom >= 128) recommendations.add("Bộ nhớ " + product.rom + "GB đủ dùng lâu dài");
                if (product.pin >= 4000) recommendations.add("Pin " + product.pin + "mAh sử dụng cả ngày");
                if (product.hz >= 120) recommendations.add("Màn hình " + product.hz + "Hz mượt mà");
                if (product.brand != null && product.brand.toLowerCase().contains("apple")) {
                    recommendations.add("iOS ổn định và bảo mật cao");
                }
                break;
            }
        }
        
        return recommendations;
    }
    
    private List<String> generateDetailedRecommendations(List<EnhancedMetrics> products) {
        List<String> recommendations = new ArrayList<>();
        
        // Find best in each category
        EnhancedMetrics bestPerformance = products.stream()
            .max((a, b) -> Double.compare(a.performanceScore, b.performanceScore))
            .orElse(null);
            
        EnhancedMetrics bestValue = products.stream()
            .filter(p -> p.price > 0)
            .min((a, b) -> Double.compare(a.price, b.price))
            .orElse(null);
            
        EnhancedMetrics bestBattery = products.stream()
            .max((a, b) -> Integer.compare(a.pin, b.pin))
            .orElse(null);
        
        if (bestPerformance != null) {
            recommendations.add("🏆 **Hiệu năng tốt nhất:** " + bestPerformance.name + 
                " (Điểm: " + String.format("%.1f", bestPerformance.performanceScore) + "/100)");
        }
        
        if (bestValue != null) {
            recommendations.add("💰 **Giá tốt nhất:** " + bestValue.name + 
                " (" + String.format("%.0f", bestValue.price/1000000) + " triệu)");
        }
        
        if (bestBattery != null) {
            recommendations.add("🔋 **Pin trâu nhất:** " + bestBattery.name + 
                " (" + bestBattery.pin + "mAh)");
        }
        
        return recommendations;
    }

    private String s(Object o) { return o == null ? null : String.valueOf(o); }
    private double d(Object o) { try { return o == null ? Double.NaN : Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return Double.NaN; } }
    private int ramGB(String s) { if (s == null) return 0; try { var m = java.util.regex.Pattern.compile("(\\d+)").matcher(s.replace(",","")); return m.find()?Integer.parseInt(m.group(1)):0; } catch (Exception e){ return 0; } }
    private int pinMAh(String s) { if (s == null) return 0; try { var m = java.util.regex.Pattern.compile("(\\d{3,5})").matcher(s.replace(",","")); return m.find()?Integer.parseInt(m.group(1)):0; } catch (Exception e){ return 0; } }
    private int hz(String s) { if (s == null) return 0; try { var m = java.util.regex.Pattern.compile("(\\d+)\\s*hz", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s); return m.find()?Integer.parseInt(m.group(1)):0; } catch (Exception e){ return 0; } }

    // Fallback inference from product name tokens, e.g., "8GB/256GB" or "8-256"
    private String inferRamFromName(String name) {
        if (name == null) return null;
        try {
            var m = java.util.regex.Pattern.compile("(\\d{1,2})\\s*(gb|g|ram)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(name);
            if (m.find()) return m.group(1) + "GB";
            // Pattern like 8/256
            m = java.util.regex.Pattern.compile("(\\d{1,2})\\s*[/|-]\\s*(\\d{2,4})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(name);
            if (m.find()) return m.group(1) + "GB";
        } catch (Exception ignore) {}
        return null;
    }

    private String inferRomFromName(String name) {
        if (name == null) return null;
        try {
            var m = java.util.regex.Pattern.compile("(\\d{2,4})\\s*(gb|g|rom)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(name);
            if (m.find()) return m.group(1) + "GB";
            // Pattern like 8/256
            m = java.util.regex.Pattern.compile("(\\d{1,2})\\s*[/|-]\\s*(\\d{2,4})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(name);
            if (m.find()) return m.group(2) + "GB";
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * Parse search criteria from user query
     */
    private SearchCriteria parseSearchCriteria(String query) {
        SearchCriteria criteria = new SearchCriteria();
        if (query == null || query.isBlank()) return criteria;
        
        String lowerQuery = query.toLowerCase();
        criteria.setKeyword(query);
        
        // Parse price range
        parsePriceRange(lowerQuery, criteria);
        
        // Parse RAM
        parseRamCriteria(lowerQuery, criteria);
        
        // Parse ROM/Storage
        parseRomCriteria(lowerQuery, criteria);
        
        // Parse OS
        parseOsCriteria(lowerQuery, criteria);
        
        // Parse brand
        parseBrandCriteria(lowerQuery, criteria);
        
        // Parse battery
        parseBatteryCriteria(lowerQuery, criteria);
        
        // Parse screen size
        parseScreenCriteria(lowerQuery, criteria);
        
        // Parse camera
        parseCameraCriteria(lowerQuery, criteria);
        
        return criteria;
    }
    
    private void parsePriceRange(String query, SearchCriteria criteria) {
        // Patterns: "dưới 10 triệu", "từ 5 đến 15 triệu", "trên 20 triệu"
        var underPattern = java.util.regex.Pattern.compile("dưới\\s+(\\d+)\\s*(triệu|tr|m)");
        var fromToPattern = java.util.regex.Pattern.compile("từ\\s+(\\d+)\\s*(triệu|tr|m)\\s*đến\\s+(\\d+)\\s*(triệu|tr|m)");
        var abovePattern = java.util.regex.Pattern.compile("trên\\s+(\\d+)\\s*(triệu|tr|m)");
        
        var underMatch = underPattern.matcher(query);
        if (underMatch.find()) {
            criteria.setMaxPrice(java.math.BigDecimal.valueOf(Long.parseLong(underMatch.group(1)) * 1000000));
        }
        
        var fromToMatch = fromToPattern.matcher(query);
        if (fromToMatch.find()) {
            criteria.setMinPrice(java.math.BigDecimal.valueOf(Long.parseLong(fromToMatch.group(1)) * 1000000));
            criteria.setMaxPrice(java.math.BigDecimal.valueOf(Long.parseLong(fromToMatch.group(3)) * 1000000));
        }
        
        var aboveMatch = abovePattern.matcher(query);
        if (aboveMatch.find()) {
            criteria.setMinPrice(java.math.BigDecimal.valueOf(Long.parseLong(aboveMatch.group(1)) * 1000000));
        }
    }
    
    private void parseRamCriteria(String query, SearchCriteria criteria) {
        var ramPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(gb|g)\\s*ram");
        var match = ramPattern.matcher(query);
        if (match.find()) {
            int ramGB = Integer.parseInt(match.group(1));
            // Map RAM GB to RAM ID (this would need to be implemented based on your data)
            criteria.setRamId(mapRamGBToId(ramGB));
        }
    }
    
    private void parseRomCriteria(String query, SearchCriteria criteria) {
        var romPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(gb|g)\\s*(rom|bộ nhớ|storage)");
        var match = romPattern.matcher(query);
        if (match.find()) {
            int romGB = Integer.parseInt(match.group(1));
            criteria.setRomId(mapRomGBToId(romGB));
        }
    }
    
    private void parseOsCriteria(String query, SearchCriteria criteria) {
        if (query.contains("android")) {
            criteria.setOsName("Android");
        } else if (query.contains("ios")) {
            criteria.setOsName("iOS");
        } else if (query.contains("one ui")) {
            criteria.setOsName("One UI");
        } else if (query.contains("miui")) {
            criteria.setOsName("MIUI");
        }
    }
    
    private void parseBrandCriteria(String query, SearchCriteria criteria) {
        if (query.contains("iphone") || query.contains("apple")) {
            criteria.setBrandName("Apple");
        } else if (query.contains("samsung") || query.contains("galaxy")) {
            criteria.setBrandName("Samsung");
        } else if (query.contains("xiaomi") || query.contains("mi") || query.contains("redmi")) {
            criteria.setBrandName("Xiaomi");
        } else if (query.contains("oppo")) {
            criteria.setBrandName("OPPO");
        } else if (query.contains("vivo")) {
            criteria.setBrandName("Vivo");
        }
    }
    
    private void parseBatteryCriteria(String query, SearchCriteria criteria) {
        var batteryPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(mah|mAh)");
        var match = batteryPattern.matcher(query);
        if (match.find()) {
            int battery = Integer.parseInt(match.group(1));
            criteria.setMinBattery(String.valueOf(battery));
        }
    }
    
    private void parseScreenCriteria(String query, SearchCriteria criteria) {
        var screenPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*inch");
        var match = screenPattern.matcher(query);
        if (match.find()) {
            double screenSize = Double.parseDouble(match.group(1));
            criteria.setMinScreen(String.valueOf(screenSize));
        }
    }
    
    private void parseCameraCriteria(String query, SearchCriteria criteria) {
        var cameraPattern = java.util.regex.Pattern.compile("(\\d+)\\s*mp\\s*camera");
        var match = cameraPattern.matcher(query);
        if (match.find()) {
            int mp = Integer.parseInt(match.group(1));
            criteria.setRearCam(String.valueOf(mp));
        }
    }
    
    private Integer mapRamGBToId(int ramGB) {
        // This would need to be implemented based on your RAM table
        // For now, return null to use string matching
        return null;
    }
    
    private Integer mapRomGBToId(int romGB) {
        // This would need to be implemented based on your ROM table
        // For now, return null to use string matching
        return null;
    }
    
    private double calculateRelevanceScore(SanPhamDTO product, SearchCriteria criteria) {
        double score = 0.0;
        
        // Name matching
        if (criteria.getKeyword() != null && product.getTenSanPham() != null) {
            String name = product.getTenSanPham().toLowerCase();
            String keyword = criteria.getKeyword().toLowerCase();
            if (name.contains(keyword)) {
                score += 10.0;
            }
        }
        
        // Brand matching
        if (criteria.getBrandName() != null && product.getTenHang() != null) {
            if (product.getTenHang().toLowerCase().contains(criteria.getBrandName().toLowerCase())) {
                score += 8.0;
            }
        }
        
        // Price range matching
        if (criteria.getMinPrice() != null && product.getGiaBan() != null) {
            if (product.getGiaBan().doubleValue() >= criteria.getMinPrice().doubleValue()) {
                score += 5.0;
            }
        }
        if (criteria.getMaxPrice() != null && product.getGiaBan() != null) {
            if (product.getGiaBan().doubleValue() <= criteria.getMaxPrice().doubleValue()) {
                score += 5.0;
            }
        }
        
        return score;
    }
    
    /**
     * Intent Analysis class for understanding user requests
     */
    private static class IntentAnalysis {
        private String type;
        private double confidence;
        private Map<String, Object> entities;
        private String context;
        
        public IntentAnalysis(String type, double confidence) {
            this.type = type;
            this.confidence = confidence;
            this.entities = new HashMap<>();
            this.context = "";
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public Map<String, Object> getEntities() { return entities; }
        public void setEntities(Map<String, Object> entities) { this.entities = entities; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
    
    /**
     * Analyze user intent from message
     */
    private IntentAnalysis analyzeUserIntent(String message) {
        if (message == null || message.isBlank()) {
            return new IntentAnalysis("unknown", 0.0);
        }
        
        String lowerMessage = message.toLowerCase();
        double confidence = 0.0;
        String type = "general";
        Map<String, Object> entities = new HashMap<>();
        
        // Product search patterns
        if (lowerMessage.contains("tìm") || lowerMessage.contains("search") || 
            lowerMessage.contains("mua") || lowerMessage.contains("điện thoại") ||
            lowerMessage.contains("smartphone") || lowerMessage.contains("phone")) {
            type = "product_search";
            confidence = 0.8;
            
            // Extract product specifications
            extractProductEntities(lowerMessage, entities);
        }
        
        // Comparison patterns
        else if (lowerMessage.contains("so sánh") || lowerMessage.contains("compare") ||
                 lowerMessage.contains("khác nhau") || lowerMessage.contains("nào tốt hơn")) {
            type = "comparison";
            confidence = 0.9;
            
            // Extract product names for comparison
            extractComparisonEntities(lowerMessage, entities);
        }
        
        // Order tracking patterns
        else if (lowerMessage.contains("đơn hàng") || lowerMessage.contains("order") ||
                 lowerMessage.contains("tra cứu") || lowerMessage.contains("mã đơn")) {
            type = "order_tracking";
            confidence = 0.9;
            
            // Extract order number and phone
            extractOrderEntities(lowerMessage, entities);
        }
        
        // Price inquiry patterns
        else if (lowerMessage.contains("giá") || lowerMessage.contains("price") ||
                 lowerMessage.contains("bao nhiêu") || lowerMessage.contains("cost")) {
            type = "price_inquiry";
            confidence = 0.7;
            
            // Extract price range
            extractPriceEntities(lowerMessage, entities);
        }
        
        // Technical specification patterns
        else if (lowerMessage.contains("ram") || lowerMessage.contains("rom") ||
                 lowerMessage.contains("pin") || lowerMessage.contains("camera") ||
                 lowerMessage.contains("màn hình") || lowerMessage.contains("chip")) {
            type = "spec_inquiry";
            confidence = 0.8;
            
            // Extract technical specs
            extractSpecEntities(lowerMessage, entities);
        }
        
        // Brand preference patterns
        else if (lowerMessage.contains("iphone") || lowerMessage.contains("samsung") ||
                 lowerMessage.contains("xiaomi") || lowerMessage.contains("oppo") ||
                 lowerMessage.contains("vivo") || lowerMessage.contains("huawei")) {
            type = "brand_preference";
            confidence = 0.7;
            
            // Extract brand preference
            extractBrandEntities(lowerMessage, entities);
        }
        
        // Greeting patterns
        else if (lowerMessage.contains("xin chào") || lowerMessage.contains("hello") ||
                 lowerMessage.contains("hi") || lowerMessage.contains("chào")) {
            type = "greeting";
            confidence = 0.9;
        }
        
        // Help patterns
        else if (lowerMessage.contains("giúp") || lowerMessage.contains("help") ||
                 lowerMessage.contains("hướng dẫn") || lowerMessage.contains("làm sao")) {
            type = "help";
            confidence = 0.8;
        }
        
        IntentAnalysis analysis = new IntentAnalysis(type, confidence);
        analysis.setEntities(entities);
        analysis.setContext(buildContextFromEntities(entities));
        
        return analysis;
    }
    
    private void extractProductEntities(String message, Map<String, Object> entities) {
        // Extract RAM
        var ramPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(gb|g)\\s*ram");
        var ramMatch = ramPattern.matcher(message);
        if (ramMatch.find()) {
            entities.put("ram", Integer.parseInt(ramMatch.group(1)));
        }
        
        // Extract ROM
        var romPattern = java.util.regex.Pattern.compile("(\\d+)\\s*(gb|g)\\s*(rom|bộ nhớ|storage)");
        var romMatch = romPattern.matcher(message);
        if (romMatch.find()) {
            entities.put("rom", Integer.parseInt(romMatch.group(1)));
        }
        
        // Extract price range
        var pricePattern = java.util.regex.Pattern.compile("(dưới|trên|từ)\\s*(\\d+)\\s*(triệu|tr|m)");
        var priceMatch = pricePattern.matcher(message);
        if (priceMatch.find()) {
            String operator = priceMatch.group(1);
            int amount = Integer.parseInt(priceMatch.group(2));
            entities.put("price_operator", operator);
            entities.put("price_amount", amount);
        }
        
        // Extract brand
        if (message.contains("iphone") || message.contains("apple")) {
            entities.put("brand", "Apple");
        } else if (message.contains("samsung") || message.contains("galaxy")) {
            entities.put("brand", "Samsung");
        } else if (message.contains("xiaomi") || message.contains("mi")) {
            entities.put("brand", "Xiaomi");
        } else if (message.contains("oppo")) {
            entities.put("brand", "OPPO");
        } else if (message.contains("vivo")) {
            entities.put("brand", "Vivo");
        }
    }
    
    private void extractComparisonEntities(String message, Map<String, Object> entities) {
        // Extract product names for comparison
        String[] words = message.split("\\s+");
        List<String> products = new ArrayList<>();
        
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("và") || words[i].equals("với") || words[i].equals("vs")) {
                if (i > 0 && i < words.length - 1) {
                    products.add(words[i-1] + " " + words[i+1]);
                }
            }
        }
        
        if (!products.isEmpty()) {
            entities.put("products_to_compare", products);
        }
    }
    
    private void extractOrderEntities(String message, Map<String, Object> entities) {
        // Extract order number
        var orderPattern = java.util.regex.Pattern.compile("(px|px\\d+|\\d{4,})", java.util.regex.Pattern.CASE_INSENSITIVE);
        var orderMatch = orderPattern.matcher(message);
        if (orderMatch.find()) {
            entities.put("order_number", orderMatch.group(1));
        }
        
        // Extract phone number
        var phonePattern = java.util.regex.Pattern.compile("(0\\d{9,10})");
        var phoneMatch = phonePattern.matcher(message);
        if (phoneMatch.find()) {
            entities.put("phone_number", phoneMatch.group(1));
        }
    }
    
    private void extractPriceEntities(String message, Map<String, Object> entities) {
        // Extract specific price mentions
        var pricePattern = java.util.regex.Pattern.compile("(\\d+)\\s*(triệu|tr|m)");
        var priceMatch = pricePattern.matcher(message);
        if (priceMatch.find()) {
            entities.put("price_mentioned", Integer.parseInt(priceMatch.group(1)));
        }
    }
    
    private void extractSpecEntities(String message, Map<String, Object> entities) {
        // Extract technical specifications
        if (message.contains("ram")) {
            entities.put("spec_type", "RAM");
        }
        if (message.contains("rom") || message.contains("bộ nhớ")) {
            entities.put("spec_type", "ROM");
        }
        if (message.contains("pin")) {
            entities.put("spec_type", "Battery");
        }
        if (message.contains("camera")) {
            entities.put("spec_type", "Camera");
        }
        if (message.contains("màn hình")) {
            entities.put("spec_type", "Screen");
        }
        if (message.contains("chip")) {
            entities.put("spec_type", "Chip");
        }
    }
    
    private void extractBrandEntities(String message, Map<String, Object> entities) {
        if (message.contains("iphone") || message.contains("apple")) {
            entities.put("preferred_brand", "Apple");
        } else if (message.contains("samsung") || message.contains("galaxy")) {
            entities.put("preferred_brand", "Samsung");
        } else if (message.contains("xiaomi") || message.contains("mi")) {
            entities.put("preferred_brand", "Xiaomi");
        } else if (message.contains("oppo")) {
            entities.put("preferred_brand", "OPPO");
        } else if (message.contains("vivo")) {
            entities.put("preferred_brand", "Vivo");
        }
    }
    
    private String buildContextFromEntities(Map<String, Object> entities) {
        StringBuilder context = new StringBuilder();
        
        if (entities.containsKey("brand")) {
            context.append("Khách hàng quan tâm đến thương hiệu: ").append(entities.get("brand")).append(". ");
        }
        
        if (entities.containsKey("ram")) {
            context.append("Yêu cầu RAM: ").append(entities.get("ram")).append("GB. ");
        }
        
        if (entities.containsKey("rom")) {
            context.append("Yêu cầu ROM: ").append(entities.get("rom")).append("GB. ");
        }
        
        if (entities.containsKey("price_amount")) {
            String operator = (String) entities.get("price_operator");
            int amount = (Integer) entities.get("price_amount");
            context.append("Ngân sách: ").append(operator).append(" ").append(amount).append(" triệu. ");
        }
        
        return context.toString();
    }
    
    private String generateSmartFallbackReply(String message, IntentAnalysis intent) {
        StringBuilder reply = new StringBuilder();
        
        switch (intent.getType()) {
            case "product_search":
                reply.append("🔍 Tôi hiểu bạn đang tìm điện thoại. ");
                if (intent.getEntities().containsKey("brand")) {
                    reply.append("Bạn quan tâm đến ").append(intent.getEntities().get("brand")).append("? ");
                }
                if (intent.getEntities().containsKey("ram")) {
                    reply.append("Với RAM ").append(intent.getEntities().get("ram")).append("GB? ");
                }
                reply.append("Hãy để tôi tìm kiếm sản phẩm phù hợp cho bạn!\n\n");
                reply.append("💡 **Gợi ý tìm kiếm:**\n");
                reply.append("• 'iPhone 15 Pro 256GB' - tìm theo tên cụ thể\n");
                reply.append("• 'dưới 15 triệu RAM 8GB' - tìm theo giá và cấu hình\n");
                reply.append("• 'Samsung camera tốt' - tìm theo thương hiệu và tính năng");
                break;
                
            case "comparison":
                reply.append("⚖️ Bạn muốn so sánh điện thoại? ");
                if (intent.getEntities().containsKey("products_to_compare")) {
                    reply.append("Tôi có thể giúp so sánh các sản phẩm này.\n\n");
                } else {
                    reply.append("Hãy cho tôi biết 2 sản phẩm bạn muốn so sánh!\n\n");
                }
                reply.append("💡 **Cách so sánh:**\n");
                reply.append("• 'So sánh iPhone 15 và Galaxy S24'\n");
                reply.append("• 'iPhone 15 Pro vs Xiaomi 14'\n");
                reply.append("• 'So sánh 2 sản phẩm đầu' (sau khi tìm kiếm)");
                break;
                
            case "price_inquiry":
                reply.append("💰 Bạn đang hỏi về giá? ");
                if (intent.getEntities().containsKey("price_mentioned")) {
                    reply.append("Với ngân sách ").append(intent.getEntities().get("price_mentioned")).append(" triệu, ");
                }
                reply.append("tôi có thể gợi ý những sản phẩm phù hợp!\n\n");
                reply.append("💡 **Tìm theo giá:**\n");
                reply.append("• 'dưới 10 triệu' - điện thoại giá rẻ\n");
                reply.append("• 'từ 15 đến 25 triệu' - tầm trung\n");
                reply.append("• 'trên 30 triệu' - cao cấp");
                break;
                
            case "spec_inquiry":
                reply.append("🔧 Bạn quan tâm đến thông số kỹ thuật? ");
                if (intent.getEntities().containsKey("spec_type")) {
                    reply.append("Về ").append(intent.getEntities().get("spec_type")).append("? ");
                }
                reply.append("Tôi có thể giải thích chi tiết!\n\n");
                reply.append("💡 **Thông số quan trọng:**\n");
                reply.append("• **RAM**: 6GB+ cho mượt mà, 8GB+ cho gaming\n");
                reply.append("• **ROM**: 128GB+ cho đủ dùng, 256GB+ cho thoải mái\n");
                reply.append("• **Pin**: 4000mAh+ cho cả ngày\n");
                reply.append("• **Camera**: 48MP+ cho ảnh đẹp");
                break;
                
            case "brand_preference":
                reply.append("🏷️ Bạn có thương hiệu yêu thích? ");
                if (intent.getEntities().containsKey("preferred_brand")) {
                    reply.append("Tôi hiểu bạn thích ").append(intent.getEntities().get("preferred_brand")).append("!\n\n");
                }
                reply.append("💡 **Các thương hiệu phổ biến:**\n");
                reply.append("• **Apple**: iOS mượt mà, camera xuất sắc\n");
                reply.append("• **Samsung**: Android đa dạng, màn hình đẹp\n");
                reply.append("• **Xiaomi**: Giá tốt, cấu hình cao\n");
                reply.append("• **OPPO/Vivo**: Camera selfie đẹp");
                break;
                
            case "greeting":
                reply.append("👋 Xin chào! Tôi là trợ lý tư vấn điện thoại thông minh.\n\n");
                reply.append("🎯 **Tôi có thể giúp bạn:**\n");
                reply.append("• 🔍 Tìm điện thoại phù hợp\n");
                reply.append("• ⚖️ So sánh sản phẩm\n");
                reply.append("• 💰 Tư vấn theo ngân sách\n");
                reply.append("• 📋 Tra cứu đơn hàng\n");
                reply.append("• 🔧 Giải thích thông số kỹ thuật\n\n");
                reply.append("Hãy cho tôi biết bạn cần gì nhé!");
                break;
                
            case "help":
                reply.append("🆘 Tôi sẵn sàng giúp đỡ bạn!\n\n");
                reply.append("📖 **Hướng dẫn sử dụng:**\n");
                reply.append("1. **Tìm kiếm**: 'iPhone 15', 'Samsung dưới 20 triệu'\n");
                reply.append("2. **So sánh**: 'So sánh iPhone 15 và Galaxy S24'\n");
                reply.append("3. **Tra cứu**: 'Mã đơn PX1234 số 0912345678'\n");
                reply.append("4. **Tư vấn**: 'Điện thoại nào tốt cho gaming?'\n\n");
                reply.append("💡 **Mẹo**: Hãy mô tả càng chi tiết càng tốt để tôi tư vấn chính xác!");
                break;
                
            default:
                reply.append("🤔 Tôi chưa hiểu rõ yêu cầu của bạn. ");
                reply.append("Bạn có thể thử:\n\n");
                reply.append("• 🔍 **Tìm sản phẩm**: 'iPhone 15 256GB'\n");
                reply.append("• ⚖️ **So sánh**: 'so sánh iPhone 15 Pro và Galaxy S24'\n");
                reply.append("• 📋 **Tra cứu đơn**: 'mã đơn PX1234 số 09xxxxxxxx'\n");
                reply.append("• 💰 **Tìm theo giá**: 'dưới 15 triệu RAM 8GB'\n");
                reply.append("• 🔧 **Hỏi thông số**: 'RAM 8GB có đủ không?'");
        }
        
        return reply.toString();
    }
    
    private List<String> generateSmartSuggestions(IntentAnalysis intent) {
        List<String> suggestions = new ArrayList<>();
        
        switch (intent.getType()) {
            case "product_search":
                suggestions.add("🔍 Tìm điện thoại theo tên cụ thể");
                suggestions.add("💰 Tìm theo ngân sách");
                suggestions.add("⚖️ So sánh sản phẩm");
                break;
            case "comparison":
                suggestions.add("⚖️ So sánh 2 sản phẩm");
                suggestions.add("🔍 Tìm thêm sản phẩm khác");
                suggestions.add("💰 So sánh giá cả");
                break;
            case "price_inquiry":
                suggestions.add("💰 Tìm theo ngân sách");
                suggestions.add("⭐ Sản phẩm giá tốt nhất");
                suggestions.add("🔍 So sánh giá");
                break;
            default:
                suggestions.add("🔍 Tìm sản phẩm");
                suggestions.add("⚖️ So sánh");
                suggestions.add("💰 Tư vấn giá");
        }
        
        return suggestions;
    }
    
    /**
     * Filter products based on search criteria
     */
    private List<SanPhamDTO> filterProductsByCriteria(List<SanPhamDTO> products, SearchCriteria criteria) {
        return products.stream()
            .filter(product -> {
                // Filter by price range
                if (criteria.getMinPrice() != null && product.getGiaBan() != null) {
                    if (product.getGiaBan().doubleValue() < criteria.getMinPrice().doubleValue()) {
                        return false;
                    }
                }
                if (criteria.getMaxPrice() != null && product.getGiaBan() != null) {
                    if (product.getGiaBan().doubleValue() > criteria.getMaxPrice().doubleValue()) {
                        return false;
                    }
                }
                
                // Filter by brand
                if (criteria.getBrandName() != null && product.getTenHang() != null) {
                    if (!product.getTenHang().toLowerCase().contains(criteria.getBrandName().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by OS
                if (criteria.getOsName() != null && product.getTenHeDieuHanh() != null) {
                    if (!product.getTenHeDieuHanh().toLowerCase().contains(criteria.getOsName().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by chip
                if (criteria.getChip() != null && product.getTenChip() != null) {
                    if (!product.getTenChip().toLowerCase().contains(criteria.getChip().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by RAM (if specified in name or specs)
                if (criteria.getRamId() != null) {
                    String ram = product.getTenRam();
                    if (ram == null || ram.isBlank()) {
                        ram = inferRamFromName(product.getTenSanPham());
                    }
                    if (ram != null) {
                        int ramGB = ramGB(ram);
                        if (ramGB < criteria.getRamId()) {
                            return false;
                        }
                    }
                }
                
                // Filter by ROM (if specified in name or specs)
                if (criteria.getRomId() != null) {
                    String rom = product.getTenRom();
                    if (rom == null || rom.isBlank()) {
                        rom = inferRomFromName(product.getTenSanPham());
                    }
                    if (rom != null) {
                        int romGB = ramGB(rom); // Reuse ramGB for ROM parsing
                        if (romGB < criteria.getRomId()) {
                            return false;
                        }
                    }
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Enhanced metrics class for product comparison
     */
    private static class EnhancedMetrics {
        String name;
        double price;
        int ram;
        int rom;
        int pin;
        int hz;
        double performanceScore;
        String brand;
        String os;
        String chip;
    }

    /**
     * Search criteria class for advanced filtering
     */
    private static class SearchCriteria {
        private String keyword;
        private java.math.BigDecimal minPrice;
        private java.math.BigDecimal maxPrice;
        private String chip;
        private Integer ramId;
        private Integer romId;
        private String osName;
        private Integer brandId;
        private String brandName;
        private String gpu;
        private String cpuName;
        private String simType;
        private String minBattery;
        private String maxBattery;
        private String minScreen;
        private String maxScreen;
        private String rearCam;
        private String frontCam;
        
        public boolean hasSpecificCriteria() {
            return minPrice != null || maxPrice != null || chip != null || ramId != null || 
                   romId != null || osName != null || brandId != null || brandName != null ||
                   gpu != null || cpuName != null || simType != null || minBattery != null ||
                   maxBattery != null || minScreen != null || maxScreen != null || 
                   rearCam != null || frontCam != null;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("keyword", keyword);
            map.put("minPrice", minPrice);
            map.put("maxPrice", maxPrice);
            map.put("chip", chip);
            map.put("ramId", ramId);
            map.put("romId", romId);
            map.put("osName", osName);
            map.put("brandId", brandId);
            map.put("brandName", brandName);
            map.put("gpu", gpu);
            map.put("cpuName", cpuName);
            map.put("simType", simType);
            map.put("minBattery", minBattery);
            map.put("maxBattery", maxBattery);
            map.put("minScreen", minScreen);
            map.put("maxScreen", maxScreen);
            map.put("rearCam", rearCam);
            map.put("frontCam", frontCam);
            return map;
        }
        
        // Getters and setters
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public java.math.BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(java.math.BigDecimal minPrice) { this.minPrice = minPrice; }
        public java.math.BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(java.math.BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        public String getChip() { return chip; }
        public void setChip(String chip) { this.chip = chip; }
        public Integer getRamId() { return ramId; }
        public void setRamId(Integer ramId) { this.ramId = ramId; }
        public Integer getRomId() { return romId; }
        public void setRomId(Integer romId) { this.romId = romId; }
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public Integer getBrandId() { return brandId; }
        public void setBrandId(Integer brandId) { this.brandId = brandId; }
        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public String getGpu() { return gpu; }
        public void setGpu(String gpu) { this.gpu = gpu; }
        public String getCpuName() { return cpuName; }
        public void setCpuName(String cpuName) { this.cpuName = cpuName; }
        public String getSimType() { return simType; }
        public void setSimType(String simType) { this.simType = simType; }
        public String getMinBattery() { return minBattery; }
        public void setMinBattery(String minBattery) { this.minBattery = minBattery; }
        public String getMaxBattery() { return maxBattery; }
        public void setMaxBattery(String maxBattery) { this.maxBattery = maxBattery; }
        public String getMinScreen() { return minScreen; }
        public void setMinScreen(String minScreen) { this.minScreen = minScreen; }
        public String getMaxScreen() { return maxScreen; }
        public void setMaxScreen(String maxScreen) { this.maxScreen = maxScreen; }
        public String getRearCam() { return rearCam; }
        public void setRearCam(String rearCam) { this.rearCam = rearCam; }
        public String getFrontCam() { return frontCam; }
        public void setFrontCam(String frontCam) { this.frontCam = frontCam; }
    }
}


