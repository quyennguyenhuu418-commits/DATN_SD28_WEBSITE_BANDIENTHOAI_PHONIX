package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.payment.CreatePaymentRequest;
import com.example.datn_sd28_2025.service.VNPayService;
import com.example.datn_sd28_2025.service.ZaloPayMockService;
import com.example.datn_sd28_2025.service.ZaloPayService;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.config.VNPayConfig;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.ChiTietHoaDonDTO;
import com.example.datn_sd28_2025.dto.OnlineOrderRequest;
import com.example.datn_sd28_2025.dto.OnlineOrderResponse;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final VNPayService vnPayService;
    private final ZaloPayService zaloPayService;
    private final ZaloPayMockService zaloPayMockService;
    private final HoaDonService hoaDonService;
    private final VNPayConfig config;
    
    // Temporary storage for pending orders before VNPay payment
    private static final Map<String, PosOrderRequest> pendingOrders = new ConcurrentHashMap<>();
    // Temporary storage for pending online orders before VNPay payment
    private static final Map<String, OnlineOrderRequest> pendingOnlineOrders = new ConcurrentHashMap<>();

    public PaymentController(VNPayService vnPayService, ZaloPayService zaloPayService, ZaloPayMockService zaloPayMockService, HoaDonService hoaDonService, VNPayConfig config) {
        this.vnPayService = vnPayService;
        this.zaloPayService = zaloPayService;
        this.zaloPayMockService = zaloPayMockService;
        this.hoaDonService = hoaDonService;
        this.config = config;
    }

    @GetMapping("/vnpay/test")
    public ResponseEntity<Map<String, Object>> testVnPaySignature() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test data similar to what VNPay returns
            Map<String, String> testParams = new HashMap<>();
            testParams.put("vnp_Amount", "1000000");
            testParams.put("vnp_BankCode", "NCB");
            testParams.put("vnp_BankTranNo", "VNP1412055");
            testParams.put("vnp_CardType", "ATM");
            testParams.put("vnp_OrderInfo", "Thanh toan don hang POS");
            testParams.put("vnp_PayDate", "20251023141959");
            testParams.put("vnp_ResponseCode", "00");
            testParams.put("vnp_TmnCode", config.getTmnCode());
            testParams.put("vnp_TransactionNo", "14120555");
            testParams.put("vnp_TransactionStatus", "00");
            testParams.put("vnp_TxnRef", "test123456");
            
            // Create hash data
            Map<String, String> sortedParams = testParams.entrySet()
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
                    String encodedValue = urlEncode(entry.getValue());
                    hashData.append(entry.getKey()).append("=").append(encodedValue).append("&");
                }
            }
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }
            
            String expectedHash = vnPayService.calculateHash(hashData.toString());
            
            result.put("success", true);
            result.put("hashData", hashData.toString());
            result.put("expectedHash", expectedHash);
            result.put("tmnCode", config.getTmnCode());
            result.put("hashSecret", config.getHashSecret().substring(0, 8) + "...");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
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
            paymentRequest.setAmount(requestData.get("amount") != null ? ((Number) requestData.get("amount")).longValue() : 0L);
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
                    hoaDonDTO.setPhuongThucThanhToan((String) hoaDonData.get("phuongThucThanhToan"));
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

            // Handle combined payment method
            String paymentMethod = orderRequest.getHoaDon().getPhuongThucThanhToan();
            double originalTotal = orderRequest.getHoaDon().getTongTien();
            double originalTotalAfterDiscount = orderRequest.getHoaDon().getTongTienSauGiam();
            
            if ("combined".equals(paymentMethod) || "Kết hợp".equals(paymentMethod)) {
                // For combined payment, frontend now sends total amount (totalAfterDiscount)
                // Store original totals in ghiChu for later restoration
                String originalInfo = String.format("ORIGINAL_TOTAL:%.0f:%.0f", originalTotal, originalTotalAfterDiscount);
                String currentGhiChu = orderRequest.getHoaDon().getGhiChu();
                orderRequest.getHoaDon().setGhiChu(currentGhiChu != null ? currentGhiChu + " | " + originalInfo : originalInfo);
            }
            
            // Store order temporarily
            String txnRef = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            pendingOrders.put(txnRef, orderRequest);
            
            // Log order data
            System.out.println("Stored order data: " + orderRequest.getHoaDon().getTongTien() + " -> " + orderRequest.getHoaDon().getTongTienSauGiam());

            // Create VNPay payment URL with the txnRef (POS - redirect to Admin port)
            String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : (xri != null ? xri : "127.0.0.1");
            String url = vnPayService.createPaymentUrlWithTxnRef(paymentRequest, ip, txnRef, config.getReturnUrlPos());
            
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

    // ONLINE website payment create – dedicated for OnlineOrderRequest
    @PostMapping("/vnpay/online-create-with-order")
    public ResponseEntity<Map<String, String>> createVnPayOnlinePayment(@RequestBody Map<String, Object> requestData,
                                                                        @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                                        @RequestHeader(value = "X-Real-IP", required = false) String xri,
                                                                        @RequestHeader(value = "Host", required = false) String host) {
        try {
            System.out.println("=== VNPay Online Payment Request ===");
            System.out.println("Request data: " + requestData);
            
            // Extract payment request
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setAmount(requestData.get("amount") != null ? ((Number) requestData.get("amount")).longValue() : 0L);
            paymentRequest.setOrderInfo((String) requestData.get("orderInfo"));
            
            // Convert order data to OnlineOrderRequest
            Map<String, Object> orderMap = (Map<String, Object>) requestData.get("order");
            
            if (orderMap == null) {
                throw new IllegalArgumentException("Order data is missing in request");
            }
            
            System.out.println("Order map from request: " + orderMap);
            
            // Convert manually to handle BigDecimal conversion properly
            OnlineOrderRequest onlineOrder = new OnlineOrderRequest();
            onlineOrder.setTenKhachHang((String) orderMap.get("tenKhachHang"));
            onlineOrder.setSoDienThoai((String) orderMap.get("soDienThoai"));
            onlineOrder.setEmail((String) orderMap.get("email"));
            onlineOrder.setDiaChi((String) orderMap.get("diaChi"));
            onlineOrder.setTinhThanh((String) orderMap.get("tinhThanh"));
            onlineOrder.setQuanHuyen((String) orderMap.get("quanHuyen"));
            onlineOrder.setPhuongThucGiaoHang((String) orderMap.get("phuongThucGiaoHang"));
            onlineOrder.setPhuongThucThanhToan((String) orderMap.get("phuongThucThanhToan"));
            onlineOrder.setGhiChu((String) orderMap.get("ghiChu"));
            onlineOrder.setLoaiHoaDon((String) orderMap.get("loaiHoaDon"));
            
            // Convert BigDecimal fields
            if (orderMap.get("tongTien") != null) {
                onlineOrder.setTongTien(new java.math.BigDecimal(orderMap.get("tongTien").toString()));
            }
            if (orderMap.get("tongTienSauGiam") != null) {
                onlineOrder.setTongTienSauGiam(new java.math.BigDecimal(orderMap.get("tongTienSauGiam").toString()));
            }
            if (orderMap.get("phiVanChuyen") != null) {
                onlineOrder.setPhiVanChuyen(new java.math.BigDecimal(orderMap.get("phiVanChuyen").toString()));
            }
            if (orderMap.get("phieuGiamGiaId") != null) {
                onlineOrder.setPhieuGiamGiaId(((Number) orderMap.get("phieuGiamGiaId")).intValue());
            }
            
            // Convert chiTietDonHang
            if (orderMap.get("chiTietDonHang") != null) {
                List<Map<String, Object>> chiTietList = (List<Map<String, Object>>) orderMap.get("chiTietDonHang");
                List<OnlineOrderRequest.ChiTietDonHangRequest> chiTietDonHang = new java.util.ArrayList<>();
                
                for (Map<String, Object> item : chiTietList) {
                    OnlineOrderRequest.ChiTietDonHangRequest chiTiet = new OnlineOrderRequest.ChiTietDonHangRequest();
                    chiTiet.setChiTietSanPhamId(((Number) item.get("chiTietSanPhamId")).intValue());
                    chiTiet.setSoLuong(((Number) item.get("soLuong")).intValue());
                    if (item.get("gia") != null) {
                        chiTiet.setGia(new java.math.BigDecimal(item.get("gia").toString()));
                    }
                    if (item.get("thanhTien") != null) {
                        chiTiet.setThanhTien(new java.math.BigDecimal(item.get("thanhTien").toString()));
                    }
                    chiTietDonHang.add(chiTiet);
                }
                onlineOrder.setChiTietDonHang(chiTietDonHang);
            }
            
            System.out.println("Converted OnlineOrderRequest: " + onlineOrder);
            
            // Store order temporarily
            String txnRef = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            pendingOnlineOrders.put(txnRef, onlineOrder);
            
            System.out.println("Stored online order with txnRef: " + txnRef);
            
            // Create VNPay payment URL with the txnRef (Online - redirect to Website port)
            String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : (xri != null ? xri : "127.0.0.1");
            String url = vnPayService.createPaymentUrlWithTxnRef(paymentRequest, ip, txnRef, config.getReturnUrl());
            
            return ResponseEntity.ok(Map.of("paymentUrl", url, "txnRef", txnRef));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating VNPay online payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi tạo thanh toán VNPay Online",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<Map<String, Object>> handleVnPayReturn(
            @RequestParam Map<String, String> allParams) {
        
        System.out.println("=== VNPay Return Called ===");
        System.out.println("All params: " + allParams);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String responseCode = allParams.get("vnp_ResponseCode");
            String transactionStatus = allParams.get("vnp_TransactionStatus");
            String txnRef = allParams.get("vnp_TxnRef");
            
            System.out.println("VNPay Return - ResponseCode: " + responseCode + ", Status: " + transactionStatus + ", TxnRef: " + txnRef);
            
            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                // Payment successful
                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("transactionNo", allParams.get("vnp_TransactionNo"));
                result.put("amount", allParams.get("vnp_Amount"));
                result.put("orderInfo", allParams.get("vnp_OrderInfo"));
                
                String orderId = null;
                
                try {
                    // Get the stored order data using txnRef
                    System.out.println("=== PENDING ORDERS DEBUG ===");
                    System.out.println("TxnRef: " + txnRef);
                    System.out.println("Pending orders size: " + pendingOrders.size());
                    System.out.println("Pending orders keys: " + pendingOrders.keySet());
                    System.out.println("Pending online orders size: " + pendingOnlineOrders.size());
                    System.out.println("Pending online orders keys: " + pendingOnlineOrders.keySet());
                    
                    // Check for online order first
                    OnlineOrderRequest storedOnlineOrder = pendingOnlineOrders.get(txnRef);
                    if (storedOnlineOrder != null) {
                        System.out.println("=== STORED ONLINE ORDER DEBUG ===");
                        System.out.println("Payment method: " + storedOnlineOrder.getPhuongThucThanhToan());
                        System.out.println("PhieuGiamGiaId: " + storedOnlineOrder.getPhieuGiamGiaId());
                        System.out.println("TongTien: " + storedOnlineOrder.getTongTien());
                        System.out.println("TongTienSauGiam: " + storedOnlineOrder.getTongTienSauGiam());
                        
                        // Add payment info to ghiChu
                        String currentGhiChu = storedOnlineOrder.getGhiChu() != null ? storedOnlineOrder.getGhiChu() : "";
                        storedOnlineOrder.setGhiChu(
                            (currentGhiChu.isEmpty() ? "" : currentGhiChu + " | ") +
                            "VNPay - " + allParams.get("vnp_TransactionNo")
                        );
                        
                        // Create the online order
                        OnlineOrderResponse savedOrder = hoaDonService.createOnlineOrder(storedOnlineOrder);
                        orderId = savedOrder.getMaHoaDon();
                        result.put("orderId", savedOrder.getMaHoaDon());
                        result.put("orderCode", savedOrder.getMaHoaDon());
                        
                        System.out.println("Online order created successfully - Code: " + savedOrder.getMaHoaDon());
                        
                        // Remove from pending online orders
                        pendingOnlineOrders.remove(txnRef);
                        
                    } else {
                        // Check for POS order
                        PosOrderRequest storedOrder = pendingOrders.get(txnRef);
                        
                        if (storedOrder != null) {
                            System.out.println("=== STORED ORDER DEBUG ===");
                        System.out.println("Payment method: " + storedOrder.getHoaDon().getPhuongThucThanhToan());
                        System.out.println("PhieuGiamGiaId: " + storedOrder.getHoaDon().getPhieuGiamGiaId());
                        System.out.println("KhachHangId: " + storedOrder.getHoaDon().getKhachHangId());
                        System.out.println("TongTien: " + storedOrder.getHoaDon().getTongTien());
                        System.out.println("TongTienSauGiam: " + storedOrder.getHoaDon().getTongTienSauGiam());
                        
                        // Handle combined payment - restore original total and add payment details
                        if ("combined".equals(storedOrder.getHoaDon().getPhuongThucThanhToan())) {
                            // Extract original totals from ghiChu
                            String ghiChu = storedOrder.getHoaDon().getGhiChu();
                            double originalTotal = 0;
                            double originalTotalAfterDiscount = 0;
                            
                            if (ghiChu != null && ghiChu.contains("ORIGINAL_TOTAL:")) {
                                try {
                                    String[] parts = ghiChu.split("ORIGINAL_TOTAL:")[1].split(":")[0].split(":");
                                    originalTotal = Double.parseDouble(parts[0]);
                                    originalTotalAfterDiscount = Double.parseDouble(parts[1]);
                                } catch (Exception e) {
                                    originalTotal = storedOrder.getHoaDon().getTongTien();
                                    originalTotalAfterDiscount = storedOrder.getHoaDon().getTongTienSauGiam();
                                }
                            }
                            
                            // Get VNPay amount
                            double vnpayAmount = (double) (Long.parseLong(allParams.get("vnp_Amount")) / 100);
                            double cashAmount = originalTotalAfterDiscount - vnpayAmount;
                            
                            // Restore original totals
                            storedOrder.getHoaDon().setTongTien(originalTotal);
                            storedOrder.getHoaDon().setTongTienSauGiam(originalTotalAfterDiscount);
                            
                            // Clean up ghiChu and add payment details
                            String cleanGhiChu = ghiChu != null ? ghiChu.replaceAll("\\| ORIGINAL_TOTAL:[^|]*", "").trim() : "";
                            storedOrder.getHoaDon().setGhiChu(
                                cleanGhiChu + 
                                " | Thanh toán kết hợp - Tiền mặt: " + String.format("%.0f", cashAmount) + 
                                " VNPay: " + String.format("%.0f", vnpayAmount) + " (TXN: " + allParams.get("vnp_TransactionNo") + ")"
                            );
                        } else {
                            // Regular VNPay payment
                            storedOrder.getHoaDon().setGhiChu(
                                (storedOrder.getHoaDon().getGhiChu() != null ? storedOrder.getHoaDon().getGhiChu() + " | " : "") +
                                "VNPay - " + allParams.get("vnp_TransactionNo")
                            );
                        }
                        
                            // Save the complete order with cart items
                            HoaDonDTO savedOrder = hoaDonService.createOrder(storedOrder);
                            orderId = savedOrder.getId().toString();
                            result.put("orderId", savedOrder.getId());
                            result.put("orderCode", savedOrder.getMaHoaDon());
                            
                            System.out.println("Order created successfully - ID: " + savedOrder.getId() + ", Code: " + savedOrder.getMaHoaDon());
                            
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
                            orderId = savedOrder.getId().toString();
                            result.put("orderId", savedOrder.getId());
                            result.put("orderCode", savedOrder.getMaHoaDon());
                            
                            System.out.println("Fallback order created - ID: " + savedOrder.getId() + ", Code: " + savedOrder.getMaHoaDon());
                        }
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error saving order: " + e.getMessage());
                    e.printStackTrace();
                    result.put("orderSaveError", "Lưu đơn hàng thất bại: " + e.getMessage());
                }
                
            } else {
                // Payment failed
                result.put("success", false);
                result.put("message", "Thanh toán thất bại");
            }
            
        } catch (Exception e) {
            System.out.println("Exception in VNPay return: " + e.getMessage());
            e.printStackTrace();
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
                    hoaDonDTO.setPhuongThucThanhToan((String) hoaDonData.get("phuongThucThanhToan"));
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
    
    private static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }
}


