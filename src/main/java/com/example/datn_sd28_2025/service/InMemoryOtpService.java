package com.example.datn_sd28_2025.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@Service
public class InMemoryOtpService {
    
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Integer> otpCounts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int OTP_COUNT_EXPIRY_HOURS = 1;
    
    public InMemoryOtpService() {
        // Cleanup expired OTPs every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredOtps, 5, 5, TimeUnit.MINUTES);
    }
    
    private static class OtpData {
        private final String otpCode;
        private final LocalDateTime createdAt;
        private final LocalDateTime expiresAt;
        
        public OtpData(String otpCode, LocalDateTime expiresAt) {
            this.otpCode = otpCode;
            this.createdAt = LocalDateTime.now();
            this.expiresAt = expiresAt;
        }
        
        public String getOtpCode() { return otpCode; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }
    
    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    public String createOtp(String email) {
        // Check if user has too many active OTPs
        Integer count = otpCounts.get(email);
        if (count != null && count >= MAX_OTP_ATTEMPTS) {
            throw new RuntimeException("Bạn đã gửi quá nhiều mã OTP. Vui lòng đợi 1 giờ trước khi thử lại.");
        }
        
        // Generate new OTP
        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Store OTP
        otpStorage.put(email, new OtpData(otpCode, expiresAt));
        
        // Increment count
        otpCounts.put(email, (count != null ? count : 0) + 1);
        
        return otpCode;
    }
    
    public boolean validateOtp(String email, String otpCode) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData != null && !otpData.isExpired() && otpData.getOtpCode().equals(otpCode)) {
            // OTP is valid, but don't remove it yet - it will be removed during password reset
            return true;
        }
        
        return false;
    }
    
    public boolean validateAndConsumeOtp(String email, String otpCode) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData != null && !otpData.isExpired() && otpData.getOtpCode().equals(otpCode)) {
            // OTP is valid, remove it (one-time use)
            otpStorage.remove(email);
            return true;
        }
        
        return false;
    }
    
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpStorage.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // Clean up old counts (reset every hour)
        otpCounts.entrySet().removeIf(entry -> {
            // This is a simple cleanup - in production you might want to track count timestamps
            return Math.random() < 0.1; // 10% chance to clean up each time
        });
    }
    
    public void cleanupUsedOtps(String email) {
        otpStorage.remove(email);
        otpCounts.remove(email);
    }
    
    public boolean hasActiveOtp(String email) {
        OtpData otpData = otpStorage.get(email);
        return otpData != null && !otpData.isExpired();
    }
    
    public long getOtpTtl(String email) {
        OtpData otpData = otpStorage.get(email);
        if (otpData != null && !otpData.isExpired()) {
            return java.time.Duration.between(LocalDateTime.now(), otpData.getExpiresAt()).getSeconds();
        }
        return 0;
    }
}
