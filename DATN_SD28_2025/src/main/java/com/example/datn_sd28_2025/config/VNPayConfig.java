package com.example.datn_sd28_2025.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.tmnCode:TEST_TMN_CODE}")
    private String tmnCode;

    @Value("${vnpay.hashSecret:TEST_HASH_SECRET}")
    private String hashSecret;

    @Value("${vnpay.returnUrl:http://localhost:5174/payment/vnpay-return}")
    private String returnUrl;
    
    @Value("${vnpay.returnUrl.pos:http://localhost:5173/payment/vnpay-return}")
    private String returnUrlPos;

    @Value("${vnpay.payUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.version:2.1.0}")
    private String version;

    @Value("${vnpay.command:pay}")
    private String command;

    @Value("${vnpay.currCode:VND}")
    private String currCode;

    public String getTmnCode() { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getReturnUrl() { return returnUrl; }
    public String getReturnUrlPos() { return returnUrlPos; }
    public String getPayUrl() { return payUrl; }
    public String getVersion() { return version; }
    public String getCommand() { return command; }
    public String getCurrCode() { return currCode; }
}







