package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🚀 Ultra Chat Service - Advanced AI & Analytics Engine
 *
 * Features:
 * - Advanced intent analysis with NLP
 * - Context-aware conversation management
 * - Smart product recommendations
 * - Multi-modal message processing
 * - Real-time analytics and tracking
 * - User behavior analysis
 * - Proactive chat capabilities
 */
@Service
public class UltraChatService {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private ProductRecommendationService recommendationService;

    @Autowired(required = false)
    private AiChatClient aiChatClient;

    // In-memory storage for demo (in production, use Redis or database)
    private final Map<String, Map<String, Object>> userContexts = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> analyticsEvents = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sessionData = new ConcurrentHashMap<>();

    // Intent patterns for advanced NLP (use Map.ofEntries for >10 entries)
    private final Map<String, List<String>> intentPatterns = Map.ofEntries(
        Map.entry("greeting", Arrays.asList("xin chào", "hi", "hello", "chào bạn", "chào", "good morning", "good afternoon")),
        Map.entry("product_search", Arrays.asList("tìm điện thoại", "mua điện thoại", "sản phẩm", "điện thoại", "phone", "smartphone")),
        Map.entry("price_inquiry", Arrays.asList("giá", "price", "tiền", "bao nhiêu", "cost", "giá cả", "chi phí")),
        Map.entry("order_tracking", Arrays.asList("đơn hàng", "order", "tra cứu", "kiểm tra đơn", "tracking", "giao hàng")),
        Map.entry("warranty", Arrays.asList("bảo hành", "warranty", "sửa chữa", "hỗ trợ", "repair", "maintenance")),
        Map.entry("comparison", Arrays.asList("so sánh", "compare", "khác nhau", "đối chiếu", "versus", "vs")),
        Map.entry("recommendation", Arrays.asList("gợi ý", "recommend", "nên mua", "tư vấn", "suggestion", "advice")),
        Map.entry("complaint", Arrays.asList("phàn nàn", "complaint", "không hài lòng", "tệ", "bad", "poor")),
        Map.entry("compliment", Arrays.asList("khen", "compliment", "tốt", "hay", "tuyệt", "good", "excellent")),
        Map.entry("escalation", Arrays.asList("gặp nhân viên", "nói chuyện với người", "hỗ trợ trực tiếp", "live agent")),
        Map.entry("goodbye", Arrays.asList("tạm biệt", "goodbye", "bye", "kết thúc", "end", "finish"))
    );

    // Entity extraction patterns
    private final Map<String, List<String>> entityPatterns = Map.ofEntries(
        Map.entry("brands", Arrays.asList("iphone", "samsung", "xiaomi", "oppo", "vivo", "huawei", "oneplus", "realme", "apple")),
        Map.entry("price_ranges", Arrays.asList("dưới 5 triệu", "5-10 triệu", "10-15 triệu", "15-20 triệu", "trên 20 triệu", "rẻ", "đắt")),
        Map.entry("use_cases", Arrays.asList("chơi game", "gaming", "chụp ảnh", "camera", "photography", "pin trâu", "battery", "công việc", "business")),
        Map.entry("features", Arrays.asList("5g", "nfc", "wireless charging", "fast charging", "waterproof", "dual sim", "expandable storage"))
    );

    /**
     * Analyze user intent with advanced NLP
     */
    public Map<String, Object> analyzeIntent(String text, String userId, String sessionId) {
        String lowerText = text.toLowerCase();

        // Find best matching intent
        String bestIntent = "unknown";
        double maxConfidence = 0.0;

        for (Map.Entry<String, List<String>> entry : intentPatterns.entrySet()) {
            String intent = entry.getKey();
            List<String> patterns = entry.getValue();

            for (String pattern : patterns) {
                if (lowerText.contains(pattern)) {
                    double confidence = calculateConfidence(lowerText, pattern);
                    if (confidence > maxConfidence) {
                        maxConfidence = confidence;
                        bestIntent = intent;
                    }
                }
            }
        }
        // Hard override: if message includes any known phone brand/model tokens, force product_search
        if (lowerText.matches(".*(iphone|apple|samsung|galaxy|xiaomi|oppo|vivo|huawei|oneplus|realme).*")) {
            bestIntent = "product_search";
            maxConfidence = Math.max(maxConfidence, 0.9);
        }

        // Extract entities
        Map<String, Object> entities = extractEntities(text);
        // Add heuristic: brand from free text tokens (iphone -> apple)
        String lower = text.toLowerCase();
        java.util.List<String> brands = new java.util.ArrayList<>();
        if (lower.contains("iphone") || lower.contains("ios")) { brands.add("apple"); brands.add("iphone"); }
        if (lower.contains("samsung") || lower.contains("galaxy")) brands.add("samsung");
        if (!brands.isEmpty()) {
            entities.putIfAbsent("brands", brands);
        }

        // If intent is unknown but entities indicate a product-related query, coerce to product_search
        if ("unknown".equals(bestIntent)) {
            boolean hasBrand = entities.containsKey("brands") && !((java.util.List<?>) entities.get("brands")).isEmpty();
            boolean hasUseCase = entities.containsKey("use_cases") && !((java.util.List<?>) entities.get("use_cases")).isEmpty();
            boolean hasPrice = entities.containsKey("prices") && !((java.util.List<?>) entities.get("prices")).isEmpty();
            if (hasBrand || hasUseCase || hasPrice) {
                bestIntent = "product_search";
                maxConfidence = Math.max(maxConfidence, 0.7);
            }
        }

        // Get user context
        Map<String, Object> userContext = getUserContext(userId, sessionId);

        // Update context with new intent
        userContext.put("lastIntent", bestIntent);
        userContext.put("lastMessage", text);
        userContext.put("lastTimestamp", LocalDateTime.now());

        return Map.of(
            "intent", bestIntent,
            "confidence", maxConfidence,
            "entities", entities,
            "context", userContext,
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * Generate intelligent response based on intent and context
     */
    public Map<String, Object> generateIntelligentResponse(String text, Map<String, Object> intent, String userId, String sessionId) {
        String intentType = (String) intent.get("intent");
        Map<String, Object> entities = (Map<String, Object>) intent.get("entities");
        Map<String, Object> context = (Map<String, Object>) intent.get("context");

        // Update user context
        updateUserContext(userId, sessionId, intent);

        // If OpenAI is enabled, try AI response first with graceful fallback
        try {
            if (aiChatClient != null && aiChatClient.isEnabled()) {
                String aiReply = aiChatClient.generateReply(text, context);
                if (aiReply != null && !aiReply.isBlank()) {
                    return Map.of("type", "text", "content", aiReply);
                }
            }
        } catch (Exception ignore) {
            // fall back to rule-based logic
        }

        switch (intentType) {
            case "greeting":
                return generateGreetingResponse(context);
            case "product_search":
                return generateProductSearchResponse(entities, context);
            case "price_inquiry":
                return generatePriceInquiryResponse(entities, context);
            case "order_tracking":
                return generateOrderTrackingResponse(entities, context);
            case "warranty":
                return generateWarrantyResponse(entities, context);
            case "comparison":
                return generateComparisonResponse(entities, context);
            case "recommendation":
                return generateRecommendationResponse(entities, context);
            case "complaint":
                return generateComplaintResponse(entities, context);
            case "compliment":
                return generateComplimentResponse(entities, context);
            case "escalation":
                return generateEscalationResponse(entities, context);
            case "goodbye":
                return generateGoodbyeResponse(entities, context);
            default:
                return generateDefaultResponse(context);
        }
    }

    /**
     * Update user context with new information
     */
    public void updateUserContext(String userId, String sessionId, Map<String, Object> intent) {
        String contextKey = userId + "_" + sessionId;
        Map<String, Object> context = userContexts.computeIfAbsent(contextKey, k -> new HashMap<>());

        // Update conversation history
        List<Map<String, Object>> history = conversationHistory.computeIfAbsent(contextKey, k -> new ArrayList<>());
        history.add(Map.of(
            "timestamp", LocalDateTime.now(),
            "intent", intent.get("intent"),
            "entities", intent.get("entities"),
            "confidence", intent.get("confidence")
        ));

        // Keep only last 10 interactions
        if (history.size() > 10) {
            history.remove(0);
        }

        // Update user preferences based on entities
        Map<String, Object> entities = (Map<String, Object>) intent.get("entities");
        if (entities.containsKey("brands")) {
            context.put("preferredBrands", entities.get("brands"));
        }
        if (entities.containsKey("price_ranges")) {
            context.put("priceRange", entities.get("price_ranges"));
        }
        if (entities.containsKey("use_cases")) {
            context.put("useCases", entities.get("use_cases"));
        }

        // Update session data
        Map<String, Object> session = sessionData.computeIfAbsent(contextKey, k -> new HashMap<>());
        session.put("lastActivity", LocalDateTime.now());
        session.put("messageCount", (Integer) session.getOrDefault("messageCount", 0) + 1);
    }

    /**
     * Get user context
     */
    public Map<String, Object> getUserContext(String userId, String sessionId) {
        String contextKey = userId + "_" + sessionId;
        return userContexts.getOrDefault(contextKey, new HashMap<>());
    }

    /**
     * Log message for analytics
     */
    public void logMessage(String userId, String sessionId, String type, Object content) {
        String contextKey = userId + "_" + sessionId;
        List<Map<String, Object>> events = analyticsEvents.computeIfAbsent(contextKey, k -> new ArrayList<>());

        events.add(Map.of(
            "timestamp", LocalDateTime.now(),
            "type", "message",
            "messageType", type,
            "content", content,
            "userId", userId,
            "sessionId", sessionId
        ));
    }

    /**
     * Log analytics event
     */
    public void logAnalyticsEvent(String userId, String sessionId, String eventName, Map<String, Object> data) {
        String contextKey = userId + "_" + sessionId;
        List<Map<String, Object>> events = analyticsEvents.computeIfAbsent(contextKey, k -> new ArrayList<>());

        events.add(Map.of(
            "timestamp", LocalDateTime.now(),
            "type", "analytics",
            "eventName", eventName,
            "data", data,
            "userId", userId,
            "sessionId", sessionId
        ));
    }

    /**
     * Get session analytics
     */
    public Map<String, Object> getSessionAnalytics(String sessionId) {
        Map<String, Object> analytics = new HashMap<>();

        // Find all events for this session
        List<Map<String, Object>> allEvents = analyticsEvents.values().stream()
            .flatMap(List::stream)
            .filter(event -> sessionId.equals(event.get("sessionId")))
            .collect(Collectors.toList());

        analytics.put("totalEvents", allEvents.size());
        analytics.put("messageCount", allEvents.stream().filter(e -> "message".equals(e.get("type"))).count());
        analytics.put("analyticsEvents", allEvents.stream().filter(e -> "analytics".equals(e.get("type"))).count());

        // Calculate session duration
        if (!allEvents.isEmpty()) {
            LocalDateTime firstEvent = (LocalDateTime) allEvents.get(0).get("timestamp");
            LocalDateTime lastEvent = (LocalDateTime) allEvents.get(allEvents.size() - 1).get("timestamp");
            analytics.put("sessionDuration", java.time.Duration.between(firstEvent, lastEvent).toMinutes());
        }

        return analytics;
    }

    /**
     * Save user feedback
     */
    public void saveFeedback(String userId, String sessionId, Integer rating, String comment) {
        String contextKey = userId + "_" + sessionId;
        List<Map<String, Object>> events = analyticsEvents.computeIfAbsent(contextKey, k -> new ArrayList<>());

        events.add(Map.of(
            "timestamp", LocalDateTime.now(),
            "type", "feedback",
            "rating", rating,
            "comment", comment,
            "userId", userId,
            "sessionId", sessionId
        ));
    }

    /**
     * Queue user for live agent
     */
    public void queueForLiveAgent(String userId, String sessionId, String reason) {
        String contextKey = userId + "_" + sessionId;
        List<Map<String, Object>> events = analyticsEvents.computeIfAbsent(contextKey, k -> new ArrayList<>());

        events.add(Map.of(
            "timestamp", LocalDateTime.now(),
            "type", "live_agent_request",
            "reason", reason,
            "userId", userId,
            "sessionId", sessionId
        ));
    }

    /**
     * Transcribe voice message (mock implementation)
     */
    public String transcribeVoice(Map<String, Object> message, String userId, String sessionId) {
        // In a real implementation, this would use speech recognition API
        // For now, return a mock transcription
        return "Xin chào, tôi muốn tìm điện thoại Samsung";
    }

    /**
     * Analyze image message (mock implementation)
     */
    public Map<String, Object> analyzeImage(Map<String, Object> message, String userId, String sessionId) {
        // In a real implementation, this would use computer vision API
        // For now, return mock analysis
        return Map.of(
            "type", "product_image",
            "confidence", 0.85,
            "products", Arrays.asList(
                Map.of("id", "samsung_galaxy_s24", "name", "Samsung Galaxy S24", "confidence", 0.9),
                Map.of("id", "iphone_15", "name", "iPhone 15", "confidence", 0.8)
            )
        );
    }

    // Helper methods
    private double calculateConfidence(String text, String pattern) {
        // Simple confidence calculation based on pattern match
        if (text.equals(pattern)) return 1.0;
        if (text.contains(pattern)) return 0.8;
        return 0.5;
    }

    private Map<String, Object> extractEntities(String text) {
        Map<String, Object> entities = new HashMap<>();
        String lowerText = text.toLowerCase();

        for (Map.Entry<String, List<String>> entry : entityPatterns.entrySet()) {
            String entityType = entry.getKey();
            List<String> patterns = entry.getValue();

            List<String> foundEntities = patterns.stream()
                .filter(lowerText::contains)
                .collect(Collectors.toList());

            if (!foundEntities.isEmpty()) {
                entities.put(entityType, foundEntities);
            }
        }

        // Extract price ranges using regex
        Pattern pricePattern = Pattern.compile("(\\d+)\\s*(triệu|tr|million|m)", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pricePattern.matcher(text);
        List<String> prices = new ArrayList<>();
        while (matcher.find()) {
            prices.add(matcher.group(1) + " " + matcher.group(2));
        }
        if (!prices.isEmpty()) {
            entities.put("prices", prices);
        }

        return entities;
    }

    // Response generators
    private Map<String, Object> generateGreetingResponse(Map<String, Object> context) {
        List<String> greetings = Arrays.asList(
            "Xin chào! Tôi là AI Assistant của PhoniX. Tôi có thể giúp bạn tìm điện thoại phù hợp, tra cứu đơn hàng, hỗ trợ bảo hành và nhiều hơn nữa! 😊",
            "Chào bạn! Tôi ở đây để hỗ trợ bạn mọi lúc. Bạn cần tư vấn gì về điện thoại không? 📱",
            "Hi! Welcome to PhoniX! Tôi có thể giúp bạn tìm sản phẩm hoàn hảo. Bạn đang tìm kiếm gì? 🔍"
        );

        return Map.of(
            "type", "text",
            "content", greetings.get(new Random().nextInt(greetings.size())),
            "quickReplies", Arrays.asList(
                Map.of("id", "product_search", "text", "🔍 Tìm điện thoại", "action", "product_search"),
                Map.of("id", "order_track", "text", "📦 Tra cứu đơn hàng", "action", "order_track"),
                Map.of("id", "warranty", "text", "🛡️ Hỗ trợ bảo hành", "action", "warranty"),
                Map.of("id", "compare", "text", "⚖️ So sánh sản phẩm", "action", "compare")
            )
        );
    }

    private Map<String, Object> generateProductSearchResponse(Map<String, Object> entities, Map<String, Object> context) {
        try {
            // Build recommendation criteria
            Map<String, Object> criteria = new HashMap<>();

            if (entities.containsKey("brands")) {
                criteria.put("brandPrefs", entities.get("brands"));
            }
            if (entities.containsKey("use_cases")) {
                criteria.put("useCases", entities.get("use_cases"));
            }
            // Also pass raw keywords for search blending
            String kw = null;
            try {
                if (context != null && context.get("lastMessage") != null) {
                    kw = String.valueOf(context.get("lastMessage"));
                }
                if ((kw == null || kw.isBlank()) && entities.containsKey("brands")) {
                    java.util.List<?> bs = (java.util.List<?>) entities.get("brands");
                    if (bs != null && !bs.isEmpty()) kw = String.valueOf(bs.get(0));
                }
            } catch (Exception ignore) {}
            if (kw != null && !kw.isBlank()) criteria.put("keywords", kw);
            if (entities.containsKey("prices")) {
                // Parse price ranges
                List<String> prices = (List<String>) entities.get("prices");
                for (String price : prices) {
                    if (price.contains("dưới 5")) {
                        criteria.put("budgetMax", 5_000_000.0);
                    } else if (price.contains("5-10")) {
                        criteria.put("budgetMin", 5_000_000.0);
                        criteria.put("budgetMax", 10_000_000.0);
                    } else if (price.contains("10-15")) {
                        criteria.put("budgetMin", 10_000_000.0);
                        criteria.put("budgetMax", 15_000_000.0);
                    } else if (price.contains("15-20")) {
                        criteria.put("budgetMin", 15_000_000.0);
                        criteria.put("budgetMax", 20_000_000.0);
                    } else if (price.contains("trên 20")) {
                        criteria.put("budgetMin", 20_000_000.0);
                    }
                }
            }

            // Debug criteria
            try {
                System.out.println("CHAT_DEBUG: criteria keywords=" + criteria.get("keywords") +
                    ", brands=" + criteria.get("brandPrefs") +
                    ", useCases=" + criteria.get("useCases"));
            } catch (Exception ignore) {}

            // Get recommendations
            List<SanPhamDTO> recommendations = recommendationService.getRecommendations(criteria);
            try { System.out.println("CHAT_DEBUG: recommendations.size=" + (recommendations!=null?recommendations.size():-1)); } catch (Exception ignore) {}

            if (recommendations != null && !recommendations.isEmpty()) {
                List<Map<String, Object>> productList = recommendations.stream()
                    .limit(3)
                    .map(this::convertToProductMap)
                    .collect(Collectors.toList());

                return Map.of(
                    "type", "product_cards",
                    "content", Map.of(
                        "title", "🎯 Sản phẩm phù hợp với bạn",
                        "products", productList
                    )
                );
            } else {
                // Fallback 1st-tier: direct store search by extracted keyword
                List<SanPhamDTO> direct = Collections.emptyList();
                try {
                    String kwLocal = null;
                    if (criteria.get("keywords") instanceof String) kwLocal = (String) criteria.get("keywords");
                    if (kwLocal != null && !kwLocal.isBlank()) {
                        direct = sanPhamService.searchSanPham(kwLocal);
                    }
                    System.out.println("CHAT_DEBUG: directSearch.size=" + (direct!=null?direct.size():-1));
                } catch (Exception ignore) {}

                if (direct != null && !direct.isEmpty()) {
                    List<Map<String, Object>> productList = direct.stream()
                        .limit(3)
                        .map(this::convertToProductMap)
                        .collect(Collectors.toList());
                    return Map.of(
                        "type", "product_cards",
                        "content", Map.of(
                            "title", "📱 Gợi ý theo từ khóa: "+ criteria.getOrDefault("keywords", ""),
                            "products", productList
                        )
                    );
                }

                // Fallback 2nd-tier: in-memory filter over active products by tokens (iphone->apple)
                try {
                    List<SanPhamDTO> active = sanPhamService.getActiveSanPham();
                    System.out.println("CHAT_DEBUG: active.size=" + (active!=null?active.size():-1));
                    List<String> tokens = new ArrayList<>();
                    Object kwObj = criteria.get("keywords");
                    if (kwObj instanceof String) {
                        String kw2 = ((String) kwObj).toLowerCase();
                        for (String t : kw2.split("[^a-z0-9áàãạăâéèêíìóòôơúùưýđ]+")) {
                            if (t != null && t.length() >= 3) tokens.add(t);
                            if ("iphone".equals(t)) tokens.add("apple");
                            if ("galaxy".equals(t)) tokens.add("samsung");
                        }
                    }
                    if (entities.containsKey("brands")) {
                        @SuppressWarnings("unchecked")
                        List<String> bs = (List<String>) entities.get("brands");
                        tokens.addAll(bs.stream().map(String::toLowerCase).toList());
                    }
                    List<SanPhamDTO> filtered = active.stream().filter(p -> {
                        String name = p.getTenSanPham() != null ? p.getTenSanPham().toLowerCase() : "";
                        String brand = p.getTenHang() != null ? p.getTenHang().toLowerCase() : "";
                        for (String t : tokens) {
                            if (name.contains(t) || brand.contains(t)) return true;
                        }
                        return false;
                    }).collect(Collectors.toList());
                    System.out.println("CHAT_DEBUG: inMemoryFiltered.size=" + filtered.size() + ", tokens=" + tokens);
                    if (!filtered.isEmpty()) {
                        List<Map<String, Object>> productList = filtered.stream().limit(3).map(this::convertToProductMap).collect(Collectors.toList());
                        return Map.of(
                            "type", "product_cards",
                            "content", Map.of(
                                "title", "📱 Gợi ý theo bộ lọc cửa hàng",
                                "products", productList
                            )
                        );
                    }
                } catch (Exception ignore) {}

                return Map.of(
                    "type", "text",
                    "content", "Xin lỗi, tôi không tìm thấy sản phẩm nào phù hợp trong kho. Hãy thử từ khóa khác hoặc chọn theo ngân sách/thương hiệu.",
                    "quickReplies", Arrays.asList(
                        Map.of("id", "budget", "text", "💰 Theo ngân sách", "action", "budget_filter"),
                        Map.of("id", "brand", "text", "🏷️ Theo thương hiệu", "action", "brand_filter"),
                        Map.of("id", "feature", "text", "⚡ Theo tính năng", "action", "feature_filter")
                    )
                );
            }
        } catch (Exception e) {
            return Map.of(
                "type", "text",
                "content", "Xin lỗi, tôi gặp sự cố khi tìm kiếm sản phẩm. Vui lòng thử lại sau."
            );
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private Map<String, Object> generatePriceInquiryResponse(Map<String, Object> entities, Map<String, Object> context) {
        if (entities.containsKey("prices")) {
            List<String> prices = (List<String>) entities.get("prices");
            return Map.of(
                "type", "text",
                "content", "Tôi hiểu bạn quan tâm đến mức giá " + String.join(", ", prices) +
                         ". Tôi sẽ tìm những sản phẩm phù hợp với ngân sách của bạn.",
                "quickReplies", Arrays.asList(
                    Map.of("id", "show_products", "text", "📱 Xem sản phẩm", "action", "show_products"),
                    Map.of("id", "compare_prices", "text", "💰 So sánh giá", "action", "compare_prices")
                )
            );
        } else {
            return Map.of(
                "type", "text",
                "content", "Bạn muốn tìm điện thoại ở mức giá nào? Tôi có thể gợi ý theo các khoảng giá khác nhau.",
                "quickReplies", Arrays.asList(
                    Map.of("id", "budget_5m", "text", "💰 Dưới 5 triệu", "action", "budget_5m"),
                    Map.of("id", "budget_10m", "text", "💰 5-10 triệu", "action", "budget_10m"),
                    Map.of("id", "budget_15m", "text", "💰 10-15 triệu", "action", "budget_15m"),
                    Map.of("id", "budget_20m", "text", "💰 15-20 triệu", "action", "budget_20m"),
                    Map.of("id", "budget_premium", "text", "💰 Trên 20 triệu", "action", "budget_premium")
                )
            );
        }
    }

    private Map<String, Object> generateOrderTrackingResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "form",
            "content", Map.of(
                "title", "📦 Tra cứu đơn hàng",
                "formType", "order_tracking",
                "fields", Arrays.asList(
                    Map.of("name", "orderCode", "label", "Mã đơn hàng", "type", "text", "required", true, "placeholder", "Nhập mã đơn hàng"),
                    Map.of("name", "phone", "label", "Số điện thoại", "type", "tel", "required", true, "placeholder", "Nhập số điện thoại")
                ),
                "submitText", "Tra cứu đơn hàng"
            )
        );
    }

    private Map<String, Object> generateWarrantyResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tôi có thể hỗ trợ bạn về bảo hành. Vui lòng cung cấp thông tin sản phẩm để tôi kiểm tra tình trạng bảo hành.",
            "quickReplies", Arrays.asList(
                Map.of("id", "warranty_check", "text", "🔍 Kiểm tra bảo hành", "action", "warranty_check"),
                Map.of("id", "warranty_claim", "text", "📋 Yêu cầu bảo hành", "action", "warranty_claim"),
                Map.of("id", "warranty_policy", "text", "📄 Chính sách bảo hành", "action", "warranty_policy")
            )
        );
    }

    private Map<String, Object> generateComparisonResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tôi có thể giúp bạn so sánh các sản phẩm. Bạn muốn so sánh những điện thoại nào?",
            "quickReplies", Arrays.asList(
                Map.of("id", "compare_flagship", "text", "🏆 So sánh flagship", "action", "compare_flagship"),
                Map.of("id", "compare_budget", "text", "💰 So sánh tầm trung", "action", "compare_budget"),
                Map.of("id", "compare_brands", "text", "🏷️ So sánh thương hiệu", "action", "compare_brands")
            )
        );
    }

    private Map<String, Object> generateRecommendationResponse(Map<String, Object> entities, Map<String, Object> context) {
        // Use context to provide personalized recommendations
        Map<String, Object> criteria = new HashMap<>();

        if (context.containsKey("preferredBrands")) {
            criteria.put("brandPrefs", context.get("preferredBrands"));
        }
        if (context.containsKey("useCases")) {
            criteria.put("useCases", context.get("useCases"));
        }
        if (context.containsKey("priceRange")) {
            criteria.put("priceRange", context.get("priceRange"));
        }

        try {
            List<SanPhamDTO> recommendations = recommendationService.getRecommendations(criteria);

            if (recommendations != null && !recommendations.isEmpty()) {
                List<Map<String, Object>> productList = recommendations.stream()
                    .limit(3)
                    .map(this::convertToProductMap)
                    .collect(Collectors.toList());

                return Map.of(
                    "type", "product_cards",
                    "content", Map.of(
                        "title", "🎯 Gợi ý dành riêng cho bạn",
                        "products", productList
                    )
                );
            }
        } catch (Exception e) {
            // Fallback to default response
        }

        return Map.of(
            "type", "text",
            "content", "Dựa trên sở thích của bạn, tôi gợi ý một số sản phẩm phổ biến:",
            "quickReplies", Arrays.asList(
                Map.of("id", "trending", "text", "🔥 Sản phẩm hot", "action", "trending"),
                Map.of("id", "best_seller", "text", "⭐ Bán chạy nhất", "action", "best_seller"),
                Map.of("id", "new_arrival", "text", "🆕 Mới ra mắt", "action", "new_arrival")
            )
        );
    }

    private Map<String, Object> generateComplaintResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tôi rất tiếc về trải nghiệm không tốt của bạn. Tôi sẽ chuyển bạn đến đội ngũ hỗ trợ chuyên nghiệp để giải quyết vấn đề này.",
            "quickReplies", Arrays.asList(
                Map.of("id", "escalate", "text", "👨‍💼 Gặp nhân viên", "action", "escalate"),
                Map.of("id", "feedback", "text", "📝 Gửi phản hồi", "action", "feedback")
            )
        );
    }

    private Map<String, Object> generateComplimentResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Cảm ơn bạn rất nhiều! Tôi rất vui khi được hỗ trợ bạn. Nếu có gì khác cần giúp đỡ, đừng ngần ngại nhé! 😊"
        );
    }

    private Map<String, Object> generateEscalationResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tôi sẽ chuyển bạn đến nhân viên hỗ trợ ngay. Vui lòng chờ trong giây lát...",
            "quickReplies", Arrays.asList(
                Map.of("id", "wait", "text", "⏳ Chờ kết nối", "action", "wait"),
                Map.of("id", "callback", "text", "📞 Yêu cầu gọi lại", "action", "callback")
            )
        );
    }

    private Map<String, Object> generateGoodbyeResponse(Map<String, Object> entities, Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tạm biệt! Cảm ơn bạn đã sử dụng dịch vụ của PhoniX. Chúc bạn một ngày tốt lành! 👋"
        );
    }

    private Map<String, Object> generateDefaultResponse(Map<String, Object> context) {
        return Map.of(
            "type", "text",
            "content", "Tôi chưa hiểu rõ yêu cầu của bạn. Bạn có thể nói rõ hơn hoặc chọn một trong các tùy chọn dưới đây:",
            "quickReplies", Arrays.asList(
                Map.of("id", "help", "text", "❓ Trợ giúp", "action", "help"),
                Map.of("id", "product_search", "text", "🔍 Tìm sản phẩm", "action", "product_search"),
                Map.of("id", "contact", "text", "📞 Liên hệ", "action", "contact")
            )
        );
    }

    private Map<String, Object> convertToProductMap(SanPhamDTO product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("name", product.getTenSanPham());
        productMap.put("imageUrl", "/images/placeholder-phone.jpg");
        productMap.put("price", 0); // Price not available in SanPhamDTO
        productMap.put("stock", product.getTongSoLuong() != null ? product.getTongSoLuong() : 0);
        productMap.put("rating", 4.5); // Default rating
        productMap.put("brand", product.getTenHang());

        List<String> specs = new ArrayList<>();
        if (product.getTenManHinh() != null) specs.add(product.getTenManHinh());
        if (product.getTenCameraSau() != null) specs.add(product.getTenCameraSau());
        if (product.getTenChip() != null) specs.add(product.getTenChip());
        if (product.getTenPin() != null) specs.add(product.getTenPin());

        productMap.put("specs", specs);
        productMap.put("features", Arrays.asList("Premium quality", "Latest technology", "Warranty included"));

        return productMap;
    }
}
