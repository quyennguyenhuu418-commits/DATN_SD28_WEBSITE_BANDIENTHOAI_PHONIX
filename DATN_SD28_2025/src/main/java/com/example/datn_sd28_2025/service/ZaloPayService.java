package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.config.ZaloPayConfig;
import com.example.datn_sd28_2025.dto.payment.CreatePaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ZaloPayService {

    @Autowired
    private ZaloPayConfig config;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPaymentUrl(CreatePaymentRequest request, String ipAddress) {
        try {
            // Prepare ZaloPay request data
            Map<String, Object> data = new HashMap<>();
            data.put("app_id", config.getAppId());
            data.put("app_user", "POS_User");
            data.put("app_time", System.currentTimeMillis());
            data.put("amount", request.getAmount());
            data.put("app_trans_id", generateAppTransId());
            data.put("item", request.getOrderInfo());
            data.put("description", "Thanh toán POS - " + request.getOrderInfo());
            data.put("embed_data", "{}");
            data.put("bank_code", "");
            data.put("callback_url", config.getCallbackUrl());
            data.put("return_url", config.getReturnUrl());

            // Generate MAC
            String mac = generateMac(data);
            data.put("mac", mac);

            // Send request to ZaloPay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            
            System.out.println("ZaloPay request data: " + data);
            System.out.println("ZaloPay URL: " + config.getCreateOrderUrl());
            
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getCreateOrderUrl(), entity, Map.class);
            
            System.out.println("ZaloPay response: " + response.getBody());

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("ZaloPay response body: " + responseBody);
                
                if (responseBody.containsKey("order_url")) {
                    return convertToString(responseBody.get("order_url"));
                } else {
                    // Log error details from ZaloPay
                    String returnCode = convertToString(responseBody.get("return_code"));
                    String subReturnCode = convertToString(responseBody.get("sub_return_code"));
                    String returnMessage = convertToString(responseBody.get("return_message"));
                    
                    System.out.println("ZaloPay error - return_code: " + returnCode + ", sub_return_code: " + subReturnCode + ", return_message: " + returnMessage);
                    throw new RuntimeException("ZaloPay error: " + returnMessage + " (Code: " + returnCode + ", SubCode: " + subReturnCode + ")");
                }
            }

            throw new RuntimeException("Không nhận được phản hồi từ ZaloPay");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo thanh toán ZaloPay: " + e.getMessage(), e);
        }
    }

    public String createPaymentUrlWithTxnRef(CreatePaymentRequest request, String ipAddress, String txnRef) {
        try {
            // Prepare ZaloPay request data
            Map<String, Object> data = new HashMap<>();
            data.put("app_id", config.getAppId());
            data.put("app_user", "POS_User");
            data.put("app_time", System.currentTimeMillis());
            data.put("amount", request.getAmount());
            data.put("app_trans_id", txnRef); // Use provided txnRef
            data.put("item", request.getOrderInfo());
            data.put("description", "Thanh toán POS - " + request.getOrderInfo());
            data.put("embed_data", "{}");
            data.put("bank_code", "");
            data.put("callback_url", config.getCallbackUrl());
            data.put("return_url", config.getReturnUrl());
            
            // Add required fields for ZaloPay
            data.put("title", "Thanh toán POS");
            data.put("currency", "VND");
            data.put("phone", "");
            data.put("email", "");
            data.put("address", "");

            // Generate MAC
            String mac = generateMac(data);
            data.put("mac", mac);

            // Send request to ZaloPay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            
            System.out.println("=== ZaloPay Request Debug ===");
            System.out.println("ZaloPay request data (with txnRef): " + data);
            System.out.println("ZaloPay URL: " + config.getCreateOrderUrl());
            System.out.println("App ID: " + config.getAppId());
            System.out.println("Key1: " + config.getKey1());
            System.out.println("Key2: " + config.getKey2());
            System.out.println("=============================");
            
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getCreateOrderUrl(), entity, Map.class);
            
            System.out.println("ZaloPay response: " + response.getBody());

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("ZaloPay response body: " + responseBody);
                
                if (responseBody.containsKey("order_url")) {
                    return convertToString(responseBody.get("order_url"));
                } else {
                    // Log error details from ZaloPay
                    String returnCode = convertToString(responseBody.get("return_code"));
                    String subReturnCode = convertToString(responseBody.get("sub_return_code"));
                    String returnMessage = convertToString(responseBody.get("return_message"));
                    
                    System.out.println("ZaloPay error - return_code: " + returnCode + ", sub_return_code: " + subReturnCode + ", return_message: " + returnMessage);
                    throw new RuntimeException("ZaloPay error: " + returnMessage + " (Code: " + returnCode + ", SubCode: " + subReturnCode + ")");
                }
            }

            throw new RuntimeException("Không nhận được phản hồi từ ZaloPay");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo thanh toán ZaloPay: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> queryOrder(String appTransId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("app_id", config.getAppId());
            data.put("app_trans_id", appTransId);

            String mac = generateMac(data);
            data.put("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getQueryOrderUrl(), entity, Map.class);

            return response.getBody() != null ? response.getBody() : new HashMap<>();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi truy vấn trạng thái ZaloPay: " + e.getMessage(), e);
        }
    }

    private String generateAppTransId() {
        // Format: yyMMdd_xxxx (4 digits random)
        java.time.LocalDate now = java.time.LocalDate.now();
        String dateStr = now.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        int random = (int) (Math.random() * 10000);
        return dateStr + "_" + String.format("%04d", random);
    }

    private String generateMac(Map<String, Object> data) {
        try {
            // Create data string for MAC calculation according to ZaloPay documentation
            // Format: app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            StringBuilder dataString = new StringBuilder();
            dataString.append(convertToString(data.get("app_id"))).append("|")
                    .append(convertToString(data.get("app_trans_id"))).append("|")
                    .append(convertToString(data.get("app_user"))).append("|")
                    .append(convertToString(data.get("amount"))).append("|")
                    .append(convertToString(data.get("app_time"))).append("|")
                    .append(convertToString(data.get("embed_data"))).append("|")
                    .append(convertToString(data.get("item")));

            System.out.println("MAC data string: " + dataString.toString());
            System.out.println("Key1: " + config.getKey1());

            // Generate HMAC SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(config.getKey1().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] macBytes = mac.doFinal(dataString.toString().getBytes(StandardCharsets.UTF_8));
            String macResult = bytesToHex(macBytes);
            System.out.println("Generated MAC: " + macResult);
            return macResult;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi tạo MAC: " + e.getMessage(), e);
        }
    }

    private String convertToString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public boolean verifyCallback(Map<String, Object> callbackData) {
        try {
            String receivedMac = convertToString(callbackData.get("mac"));
            if (receivedMac == null) {
                return false;
            }

            // Remove mac from data for verification
            Map<String, Object> dataForMac = new HashMap<>(callbackData);
            dataForMac.remove("mac");

            String calculatedMac = generateMac(dataForMac);
            return receivedMac.equals(calculatedMac);

        } catch (Exception e) {
            return false;
        }
    }
}
