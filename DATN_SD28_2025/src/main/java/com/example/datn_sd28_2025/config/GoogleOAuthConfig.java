package com.example.datn_sd28_2025.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthConfig {
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
    
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
}