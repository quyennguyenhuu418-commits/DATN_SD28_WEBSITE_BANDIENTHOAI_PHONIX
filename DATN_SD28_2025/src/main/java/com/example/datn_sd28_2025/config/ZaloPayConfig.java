package com.example.datn_sd28_2025.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zalopay")
public class ZaloPayConfig {
    private String appId;
    private String key1;
    private String key2;
    private String createOrderUrl;
    private String queryOrderUrl;
    private String returnUrl;
    private String callbackUrl;

    // Getters and Setters
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getCreateOrderUrl() {
        return createOrderUrl;
    }

    public void setCreateOrderUrl(String createOrderUrl) {
        this.createOrderUrl = createOrderUrl;
    }

    public String getQueryOrderUrl() {
        return queryOrderUrl;
    }

    public void setQueryOrderUrl(String queryOrderUrl) {
        this.queryOrderUrl = queryOrderUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}















