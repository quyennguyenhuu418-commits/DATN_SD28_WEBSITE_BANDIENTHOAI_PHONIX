package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.config.VNPayConfig;
import com.example.datn_sd28_2025.dto.payment.CreatePaymentRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    private final VNPayConfig config;

    public VNPayService(VNPayConfig config) {
        this.config = config;
    }

    public String createPaymentUrl(CreatePaymentRequest request, String ipAddress) {
        return createPaymentUrlWithTxnRef(request, ipAddress, UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    }

    public String createPaymentUrlWithTxnRef(CreatePaymentRequest request, String ipAddress, String txnRef) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", config.getVersion());
        vnpParams.put("vnp_Command", config.getCommand());
        vnpParams.put("vnp_TmnCode", config.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100));
        vnpParams.put("vnp_CurrCode", config.getCurrCode());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo() != null ? request.getOrderInfo() : "POS Payment");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", config.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        vnpParams.put("vnp_OrderType", "other");
        if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
            vnpParams.put("vnp_BankCode", request.getBankCode());
        }

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String name : fieldNames) {
            String value = vnpParams.get(name);
            if (value != null && value.length() > 0) {
                // VNPay sample hashes URL-encoded values
                hashData.append(name).append("=").append(urlEncode(value)).append("&");
                query.append(name).append("=").append(urlEncode(value)).append("&");
            }
        }
        // Remove trailing &
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        // Add hash type parameter to query only (not part of hash)
        query.append("vnp_SecureHashType=").append("HmacSHA512").append("&");
        String secureHash = hmacSHA512(config.getHashSecret(), hashData.toString());
        query.append("vnp_SecureHash=").append(secureHash);
        return config.getPayUrl() + "?" + query;
    }

    private static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }

    private static String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot sign data", e);
        }
    }

    public String calculateHash(String data) {
        return hmacSHA512(config.getHashSecret(), data);
    }
}


