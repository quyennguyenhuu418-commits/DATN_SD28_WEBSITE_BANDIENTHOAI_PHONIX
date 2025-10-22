package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.payment.CreatePaymentRequest;
import com.example.datn_sd28_2025.service.VNPayService;
import com.example.datn_sd28_2025.service.ZaloPayMockService;
import com.example.datn_sd28_2025.service.ZaloPayService;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.ChiTietHoaDonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final VNPayService vnPayService;
    private final ZaloPayService zaloPayService;
    private final ZaloPayMockService zaloPayMockService;
    private final HoaDonService hoaDonService;
    
    // Temporary storage for pending orders before VNPay payment
    private static final Map<String, PosOrderRequest> pendingOrders = new ConcurrentHashMap<>();

    public PaymentController(VNPayService vnPayService, ZaloPayService zaloPayService, ZaloPayMockService zaloPayMockService, HoaDonService hoaDonService) {
        this.vnPayService = vnPayService;
        this.zaloPayService = zaloPayService;
        this.zaloPayMockService = zaloPayMockService;
        this.hoaDonService = hoaDonService;
    }

    @GetMapping("/banks")
    public ResponseEntity<List<Map<String, String>>> getTransferBanks() {
        // Banks that have common public APIs or are widely supported via VNPay/VietQR
        List<Map<String, String>> banks = Arrays.asList(
                Map.of("code", "VCB", "name", "Vietcombank"),
                Map.of("code", "TCB", "name", "Techcombank"),
                Map.of("code", "BIDV", "name", "BIDV"),
                Map.of("code", "VTB", "name", "VietinBank"),
                Map.of("code", "ACB", "name", "ACB"),
                Map.of("code", "MB", "name", "MB Bank"),
                Map.of("code", "VPB", "name", "VPBank"),
                Map.of("code", "TPB", "name", "TPBank")
        );
        return ResponseEntity.ok(banks);
    }

    @PostMapping("/vnpay/create")
    public ResponseEntity<Map<String, String>> createVnPayPayment(@RequestBody CreatePaymentRequest request,
                                                                  @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                                  @RequestHeader(value = "X-Real-IP", required = false) String xri,
                                                                  @RequestHeader(value = "Host", required = false) String host) {
        String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : (xri != null ? xri : "127.0.0.1");
        String url = vnPayService.createPaymentUrl(request, ip);
        return ResponseEntity.ok(Map.of("paymentUrl", url));
    }

    @PostMapping("/vnpay/create-with-order")
    public ResponseEntity<Map<String, String>> createVnPayPaymentWithOrder(@RequestBody Map<String, Object> requestData,
                                                                          @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                                          @RequestHeader(value = "X-Real-IP", required = false) String xri,
                                                                          @RequestHeader(value = "Host", required = false) String host) {
        try {
            // Log incoming request
            System.out.println("VNPay create payment request: " + requestData);
            
            // Extract payment request
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setAmount(((Number) requestData.get("amount")).longValue());
            paymentRequest.setOrderInfo((String) requestData.get("orderInfo"));
            paymentRequest.setBankCode((String) requestData.get("bankCode"));

            // Extract order data
            PosOrderRequest orderRequest = new PosOrderRequest();
            if (requestData.containsKey("order")) {
                Map<String, Object> orderData = (Map<String, Object>) requestData.get("order");
                
                // Create HoaDonDTO
                HoaDonDTO hoaDonDTO = new HoaDonDTO();
                if (orderData.containsKey("hoaDon")) {
                    Map<String, Object> hoaDonData = (Map<String, Object>) orderData.get("hoaDon");
                    hoaDonDTO.setKhachHangId(hoaDonData.get("khachHangId") != null ? (Integer) hoaDonData.get("khachHangId") : null);
                    hoaDonDTO.setTongTien(hoaDonData.get("tongTien") != null ? ((Number) hoaDonData.get("tongTien")).doubleValue() : 0.0);
                    hoaDonDTO.setTongTienSauGiam(hoaDonData.get("tongTienSauGiam") != null ? ((Number) hoaDonData.get("tongTienSauGiam")).doubleValue() : 0.0);
                    hoaDonDTO.setLoaiHoaDon((String) hoaDonData.get("loaiHoaDon"));
                    hoaDonDTO.setGhiChu((String) hoaDonData.get("ghiChu"));
                    hoaDonDTO.setTenKhachHang((String) hoaDonData.get("tenKhachHang"));
                    hoaDonDTO.setSoDienThoai((String) hoaDonData.get("soDienThoai"));
                    hoaDonDTO.setDiaChi((String) hoaDonData.get("diaChi"));
                    hoaDonDTO.setPhieuGiamGiaId(hoaDonData.get("phieuGiamGiaId") != null ? (Integer) hoaDonData.get("phieuGiamGiaId") : null);
                }
                orderRequest.setHoaDon(hoaDonDTO);

                // Create ChiTietHoaDon list
                if (orderData.containsKey("chiTietHoaDon")) {
                    List<Map<String, Object>> chiTietData = (List<Map<String, Object>>) orderData.get("chiTietHoaDon");
                    List<ChiTietHoaDonDTO> chiTietList = new java.util.ArrayList<>();
                    
                    for (Map<String, Object> item : chiTietData) {
                        ChiTietHoaDonDTO chiTiet = new ChiTietHoaDonDTO();
                        chiTiet.setSanPhamId((Integer) item.get("sanPhamId"));
                        chiTiet.setSoLuong((Integer) item.get("soLuong"));
                        chiTiet.setDonGia(item.get("donGia") != null ? ((Number) item.get("donGia")).doubleValue() : 0.0);
                        chiTiet.setThanhTien(item.get("thanhTien") != null ? ((Number) item.get("thanhTien")).doubleValue() : 0.0);
                        
                        // Handle selectedImeis
                        if (item.containsKey("selectedImeis") && item.get("selectedImeis") != null) {
                            List<String> selectedImeis = (List<String>) item.get("selectedImeis");
                            chiTiet.setSelectedImeis(selectedImeis);
                        }
                        
                        chiTietList.add(chiTiet);
                    }
                    orderRequest.setChiTietHoaDon(chiTietList);
                }
            }

            // Store order temporarily
            String txnRef = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            pendingOrders.put(txnRef, orderRequest);
            
            // Log order data
            System.out.println("Stored order data: " + orderRequest.getHoaDon().getTongTien() + " -> " + orderRequest.getHoaDon().getTongTienSauGiam());

            // Create VNPay payment URL with the txnRef
            String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : (xri != null ? xri : "127.0.0.1");
            String url = vnPayService.createPaymentUrlWithTxnRef(paymentRequest, ip, txnRef);
            
            return ResponseEntity.ok(Map.of("paymentUrl", url, "txnRef", txnRef));

        } catch (Exception e) {
            e.printStackTrace(); // Log the full error
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo thanh toán VNPay", 
                "message", e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<Map<String, Object>> handleVnPayReturn(
            @RequestParam Map<String, String> allParams) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Verify signature
            String secureHash = allParams.remove("vnp_SecureHash");
            String secureHashType = allParams.remove("vnp_SecureHashType");
            
            if (secureHash == null || secureHash.isEmpty()) {
                result.put("success", false);
                result.put("message", "Missing signature");
                return ResponseEntity.ok(result);
            }
            
            // Sort parameters and build hash data
            Map<String, String> sortedParams = allParams.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
            
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }
            
            // Verify hash
            String expectedHash = vnPayService.calculateHash(hashData.toString());
            boolean isValidSignature = secureHash.equals(expectedHash);
            
            String responseCode = allParams.get("vnp_ResponseCode");
            String transactionStatus = allParams.get("vnp_TransactionStatus");
            
            if (isValidSignature && "00".equals(responseCode) && "00".equals(transactionStatus)) {
                // Payment successful
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("transactionNo", allParams.get("vnp_TransactionNo"));
                result.put("amount", allParams.get("vnp_Amount"));
                result.put("orderInfo", allParams.get("vnp_OrderInfo"));
                
                // Save order to database
                String txnRef = allParams.get("vnp_TxnRef");
                result.put("txnRef", txnRef);
                
                try {
                    // Get the stored order data using txnRef
                    PosOrderRequest storedOrder = pendingOrders.get(txnRef);
                    
                    if (storedOrder != null) {
                        // Update order with payment info
                        storedOrder.getHoaDon().setGhiChu(
                            (storedOrder.getHoaDon().getGhiChu() != null ? storedOrder.getHoaDon().getGhiChu() + " | " : "") +
                            "VNPay - " + allParams.get("vnp_TransactionNo")
                        );
                        
                        // Save the complete order with cart items
                        System.out.println("Creating order with data: " + storedOrder.getHoaDon().getTongTien() + " -> " + storedOrder.getHoaDon().getTongTienSauGiam());
                        HoaDonDTO savedOrder = hoaDonService.createOrder(storedOrder);
                        System.out.println("Order created successfully: " + savedOrder.getMaHoaDon() + " - Total: " + savedOrder.getTongTien() + " -> " + savedOrder.getTongTienSauGiam());
                        result.put("orderId", savedOrder.getId());
                        result.put("orderCode", savedOrder.getMaHoaDon());
                        
                        // Remove from pending orders
                        pendingOrders.remove(txnRef);
                        
                    } else {
                        // Fallback: create a simple order if no stored data
                        HoaDonDTO hoaDonDTO = new HoaDonDTO();
                        hoaDonDTO.setTongTien((double) (Long.parseLong(allParams.get("vnp_Amount")) / 100));
                        hoaDonDTO.setTongTienSauGiam((double) (Long.parseLong(allParams.get("vnp_Amount")) / 100));
                        hoaDonDTO.setLoaiHoaDon("NORMAL");
                        hoaDonDTO.setTenKhachHang("Khách VNPay");
                        hoaDonDTO.setSoDienThoai("VNPay");
                        hoaDonDTO.setGhiChu("Thanh toán VNPay - " + allParams.get("vnp_OrderInfo"));
                        
                        PosOrderRequest fallbackOrder = new PosOrderRequest();
                        fallbackOrder.setHoaDon(hoaDonDTO);
                        fallbackOrder.setChiTietHoaDon(new java.util.ArrayList<>());
                        
                        HoaDonDTO savedOrder = hoaDonService.createOrder(fallbackOrder);
                        result.put("orderId", savedOrder.getId());
                        result.put("orderCode", savedOrder.getMaHoaDon());
                        result.put("warning", "Đơn hàng được tạo không có chi tiết sản phẩm");
                    }
                    
                } catch (Exception e) {
                    result.put("orderSaveError", "Lưu đơn hàng thất bại: " + e.getMessage());
                    // Still return success for payment, but note the order save issue
                }
                
            } else {
                result.put("success", false);
                result.put("message", "Thanh toán thất bại");
                if (!isValidSignature) {
                    result.put("message", "Chữ ký không hợp lệ");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi xử lý thanh toán: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    // ZaloPay endpoints
    @PostMapping("/zalopay/create-with-order")
    public ResponseEntity<Map<String, String>> createZaloPayPaymentWithOrder(@RequestBody Map<String, Object> requestData,
                                                                             @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                                             @RequestHeader(value = "X-Real-IP", required = false) String xri,
                                                                             @RequestHeader(value = "Host", required = false) String host) {
        try {
            // Log incoming request
            System.out.println("ZaloPay create payment request: " + requestData);
            
            // Extract payment request
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setAmount(((Number) requestData.get("amount")).longValue());
            paymentRequest.setOrderInfo((String) requestData.get("orderInfo"));

            // Extract order data
            PosOrderRequest orderRequest = new PosOrderRequest();
            if (requestData.containsKey("order")) {
                Map<String, Object> orderData = (Map<String, Object>) requestData.get("order");
                
                // Create HoaDonDTO
                HoaDonDTO hoaDonDTO = new HoaDonDTO();
                if (orderData.containsKey("hoaDon")) {
                    Map<String, Object> hoaDonData = (Map<String, Object>) orderData.get("hoaDon");
                    hoaDonDTO.setKhachHangId(hoaDonData.get("khachHangId") != null ? (Integer) hoaDonData.get("khachHangId") : null);
                    hoaDonDTO.setTongTien(hoaDonData.get("tongTien") != null ? ((Number) hoaDonData.get("tongTien")).doubleValue() : 0.0);
                    hoaDonDTO.setTongTienSauGiam(hoaDonData.get("tongTienSauGiam") != null ? ((Number) hoaDonData.get("tongTienSauGiam")).doubleValue() : 0.0);
                    hoaDonDTO.setLoaiHoaDon((String) hoaDonData.get("loaiHoaDon"));
                    hoaDonDTO.setGhiChu((String) hoaDonData.get("ghiChu"));
                    hoaDonDTO.setTenKhachHang((String) hoaDonData.get("tenKhachHang"));
                    hoaDonDTO.setSoDienThoai((String) hoaDonData.get("soDienThoai"));
                    hoaDonDTO.setDiaChi((String) hoaDonData.get("diaChi"));
                    hoaDonDTO.setPhieuGiamGiaId(hoaDonData.get("phieuGiamGiaId") != null ? (Integer) hoaDonData.get("phieuGiamGiaId") : null);
                }
                orderRequest.setHoaDon(hoaDonDTO);

                // Create ChiTietHoaDon list
                if (orderData.containsKey("chiTietHoaDon")) {
                    List<Map<String, Object>> chiTietData = (List<Map<String, Object>>) orderData.get("chiTietHoaDon");
                    List<ChiTietHoaDonDTO> chiTietList = new java.util.ArrayList<>();
                    
                    for (Map<String, Object> item : chiTietData) {
                        ChiTietHoaDonDTO chiTiet = new ChiTietHoaDonDTO();
                        chiTiet.setSanPhamId((Integer) item.get("sanPhamId"));
                        chiTiet.setSoLuong((Integer) item.get("soLuong"));
                        chiTiet.setDonGia(item.get("donGia") != null ? ((Number) item.get("donGia")).doubleValue() : 0.0);
                        chiTiet.setThanhTien(item.get("thanhTien") != null ? ((Number) item.get("thanhTien")).doubleValue() : 0.0);
                        
                        // Handle selectedImeis
                        if (item.containsKey("selectedImeis") && item.get("selectedImeis") != null) {
                            List<String> selectedImeis = (List<String>) item.get("selectedImeis");
                            chiTiet.setSelectedImeis(selectedImeis);
                        }
                        
                        chiTietList.add(chiTiet);
                    }
                    orderRequest.setChiTietHoaDon(chiTietList);
                }
            }

            // Store order temporarily
            String txnRef = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            pendingOrders.put(txnRef, orderRequest);
            
            // Log order data
            System.out.println("Stored ZaloPay order data: " + orderRequest.getHoaDon().getTongTien() + " -> " + orderRequest.getHoaDon().getTongTienSauGiam());

                   // Create ZaloPay payment URL with the txnRef (using mock service for testing)
                   String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : (xri != null ? xri : "127.0.0.1");
                   String url = zaloPayMockService.createPaymentUrlWithTxnRef(paymentRequest, ip, txnRef);
            
            return ResponseEntity.ok(Map.of("paymentUrl", url, "txnRef", txnRef));

        } catch (Exception e) {
            e.printStackTrace(); // Log the full error
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo thanh toán ZaloPay", 
                "message", e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/zalopay/return")
    public ResponseEntity<Map<String, Object>> handleZaloPayReturn(
            @RequestParam Map<String, String> allParams) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String appTransId = allParams.get("app_trans_id");
            String status = allParams.get("status");
            
            if (appTransId == null || appTransId.isEmpty()) {
                result.put("success", false);
                result.put("message", "Missing app_trans_id");
                return ResponseEntity.ok(result);
            }
            
                   // Query order status from ZaloPay (using mock service for testing)
                   Map<String, Object> orderStatus = zaloPayMockService.queryOrder(appTransId);
            
            if ("1".equals(status) && orderStatus.containsKey("return_code") && "1".equals(orderStatus.get("return_code"))) {
                // Payment successful
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("appTransId", appTransId);
                result.put("amount", orderStatus.get("amount"));
                result.put("description", orderStatus.get("description"));
                
                // Save order to database
                try {
                    // Get the stored order data using appTransId
                    PosOrderRequest storedOrder = pendingOrders.get(appTransId);
                    
                    if (storedOrder != null) {
                        // Update order with payment info
                        storedOrder.getHoaDon().setGhiChu(
                            (storedOrder.getHoaDon().getGhiChu() != null ? storedOrder.getHoaDon().getGhiChu() + " | " : "") +
                            "ZaloPay - " + appTransId
                        );
                        
                        // Save the complete order with cart items
                        System.out.println("Creating ZaloPay order with data: " + storedOrder.getHoaDon().getTongTien() + " -> " + storedOrder.getHoaDon().getTongTienSauGiam());
                        HoaDonDTO savedOrder = hoaDonService.createOrder(storedOrder);
                        System.out.println("ZaloPay order created successfully: " + savedOrder.getMaHoaDon() + " - Total: " + savedOrder.getTongTien() + " -> " + savedOrder.getTongTienSauGiam());
                        result.put("orderId", savedOrder.getId());
                        result.put("orderCode", savedOrder.getMaHoaDon());
                        
                        // Remove from pending orders
                        pendingOrders.remove(appTransId);
                        
                    } else {
                        // Fallback: create a simple order if no stored data
                        HoaDonDTO hoaDonDTO = new HoaDonDTO();
                        hoaDonDTO.setTongTien((double) ((Number) orderStatus.get("amount")).longValue());
                        hoaDonDTO.setTongTienSauGiam((double) ((Number) orderStatus.get("amount")).longValue());
                        hoaDonDTO.setLoaiHoaDon("NORMAL");
                        hoaDonDTO.setTenKhachHang("Khách ZaloPay");
                        hoaDonDTO.setSoDienThoai("ZaloPay");
                        hoaDonDTO.setGhiChu("Thanh toán ZaloPay - " + orderStatus.get("description"));
                        
                        PosOrderRequest fallbackOrder = new PosOrderRequest();
                        fallbackOrder.setHoaDon(hoaDonDTO);
                        fallbackOrder.setChiTietHoaDon(new java.util.ArrayList<>());
                        
                        HoaDonDTO savedOrder = hoaDonService.createOrder(fallbackOrder);
                        result.put("orderId", savedOrder.getId());
                        result.put("orderCode", savedOrder.getMaHoaDon());
                        result.put("warning", "Đơn hàng được tạo không có chi tiết sản phẩm");
                    }
                    
                } catch (Exception e) {
                    result.put("orderSaveError", "Lưu đơn hàng thất bại: " + e.getMessage());
                    // Still return success for payment, but note the order save issue
                }
                
            } else {
                result.put("success", false);
                result.put("message", "Thanh toán thất bại");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi xử lý thanh toán: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<Map<String, Object>> handleZaloPayCallback(@RequestBody Map<String, Object> callbackData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
                   // Verify callback signature (using mock service for testing)
                   boolean isValidCallback = zaloPayMockService.verifyCallback(callbackData);
            
            if (isValidCallback) {
                // Process callback data
                result.put("success", true);
                result.put("message", "Callback processed successfully");
            } else {
                result.put("success", false);
                result.put("message", "Invalid callback signature");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error processing callback: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}


