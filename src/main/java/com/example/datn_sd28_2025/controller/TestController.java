package com.example.datn_sd28_2025.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Test Email - PhoniX POS System");
            message.setText("Đây là email test từ hệ thống PhoniX POS.\n\n" +
                          "Nếu bạn nhận được email này, có nghĩa là hệ thống email đang hoạt động bình thường.\n\n" +
                          "Thời gian gửi: " + java.time.LocalDateTime.now().toString() + "\n\n" +
                          "Cảm ơn bạn đã sử dụng PhoniX POS!");
            
            mailSender.send(message);
            
            return ResponseEntity.ok(Map.of(
                "message", "Email test đã được gửi thành công",
                "toEmail", toEmail,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            System.err.println("Error sending test email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Không thể gửi email test: " + e.getMessage()
            ));
        }
    }
}
























