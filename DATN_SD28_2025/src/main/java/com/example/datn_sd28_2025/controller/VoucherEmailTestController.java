package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/voucher-email")
@CrossOrigin(origins = "*")
public class VoucherEmailTestController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send-voucher-notification")
    public ResponseEntity<Map<String, String>> testVoucherNotification(@RequestParam String toEmail) {
        try {
            emailService.sendVoucherNotification(
                toEmail,
                "Nguyễn Văn Test",
                "VOUCHER123",
                "Voucher giảm giá 20%",
                "20%",
                "2024-01-01",
                "2024-12-31"
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Voucher notification email sent successfully",
                "toEmail", toEmail,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            System.err.println("Error sending voucher notification test email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Không thể gửi email test: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/send-voucher-expired")
    public ResponseEntity<Map<String, String>> testVoucherExpired(@RequestParam String toEmail) {
        try {
            emailService.sendVoucherExpiredNotification(
                toEmail,
                "Nguyễn Văn Test",
                "VOUCHER123",
                "Voucher giảm giá 20%"
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Voucher expired notification email sent successfully",
                "toEmail", toEmail,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            System.err.println("Error sending voucher expired test email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Không thể gửi email test: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/send-voucher-updated")
    public ResponseEntity<Map<String, String>> testVoucherUpdated(@RequestParam String toEmail) {
        try {
            emailService.sendVoucherUpdatedNotification(
                toEmail,
                "Nguyễn Văn Test",
                "VOUCHER123",
                "Voucher giảm giá 20%",
                "20%",
                "2024-01-01",
                "2024-12-31"
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Voucher updated notification email sent successfully",
                "toEmail", toEmail,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            System.err.println("Error sending voucher updated test email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Không thể gửi email test: " + e.getMessage()
            ));
        }
    }
}
