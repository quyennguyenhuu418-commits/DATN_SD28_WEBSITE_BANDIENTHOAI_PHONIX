package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.entity.OtpCode;
import com.example.datn_sd28_2025.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    
    @Autowired
    private OtpCodeRepository otpCodeRepository;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 3;
    
    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    public OtpCode createOtp(String email) {
        // Check if user has too many active OTPs
        List<OtpCode> activeOtps = otpCodeRepository.findActiveOtpsByEmail(email, LocalDateTime.now());
        if (activeOtps.size() >= MAX_OTP_ATTEMPTS) {
            throw new RuntimeException("Bạn đã gửi quá nhiều mã OTP. Vui lòng đợi 10 phút trước khi thử lại.");
        }
        
        // Generate new OTP
        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Create OTP entity
        OtpCode otp = OtpCode.builder()
                .email(email)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        
        return otpCodeRepository.save(otp);
    }
    
    public boolean validateOtp(String email, String otpCode) {
        Optional<OtpCode> otp = otpCodeRepository.findValidOtp(email, otpCode, LocalDateTime.now());
        
        if (otp.isPresent()) {
            // Mark OTP as used
            OtpCode otpEntity = otp.get();
            otpEntity.setIsUsed(true);
            otpEntity.setUsedAt(LocalDateTime.now());
            otpCodeRepository.save(otpEntity);
            return true;
        }
        
        return false;
    }
    
    public void cleanupExpiredOtps() {
        otpCodeRepository.deleteExpiredOtps(LocalDateTime.now());
    }
    
    public void cleanupUsedOtps(String email) {
        otpCodeRepository.deleteUsedOtpsByEmail(email);
    }
}
