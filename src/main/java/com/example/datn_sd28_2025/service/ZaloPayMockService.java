package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.config.ZaloPayConfig;
import com.example.datn_sd28_2025.dto.payment.CreatePaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ZaloPayMockService {

    @Autowired
    private ZaloPayConfig config;

    public String createPaymentUrlWithTxnRef(CreatePaymentRequest request, String ipAddress, String txnRef) {
        try {
            // Create mock ZaloPay response for testing
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("return_code", 1);
            mockResponse.put("return_message", "Thành công");
            mockResponse.put("sub_return_code", 1);
            mockResponse.put("sub_return_message", "Thành công");
            mockResponse.put("order_url", "https://sb-openapi.zalopay.vn/v2/pay/order?app_id=" + config.getAppId() + "&app_trans_id=" + txnRef + "&mac=MOCK_MAC");
            mockResponse.put("zp_trans_id", "ZP" + System.currentTimeMillis());
            mockResponse.put("order_token", "MOCK_TOKEN_" + System.currentTimeMillis());
            
            System.out.println("=== ZaloPay MOCK Service ===");
            System.out.println("Mock response: " + mockResponse);
            System.out.println("Payment URL: " + mockResponse.get("order_url"));
            System.out.println("===========================");
            
            return (String) mockResponse.get("order_url");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo thanh toán ZaloPay Mock: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> queryOrder(String appTransId) {
        // Mock successful payment
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("return_code", 1);
        mockResponse.put("return_message", "Thành công");
        mockResponse.put("sub_return_code", 1);
        mockResponse.put("sub_return_message", "Thành công");
        mockResponse.put("amount", 100000);
        mockResponse.put("description", "Mock ZaloPay Payment");
        mockResponse.put("zp_trans_id", "ZP" + System.currentTimeMillis());
        
        System.out.println("ZaloPay Mock query order: " + mockResponse);
        return mockResponse;
    }

    public boolean verifyCallback(Map<String, Object> callbackData) {
        // Mock callback verification - always return true for testing
        System.out.println("ZaloPay Mock callback verification: " + callbackData);
        return true;
    }
}
