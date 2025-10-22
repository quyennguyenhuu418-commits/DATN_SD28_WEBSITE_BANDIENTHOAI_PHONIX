package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.util.PasswordEncoderUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class PasswordTestController {
    
    @PostMapping("/encode-password")
    public Map<String, Object> encodePassword(@RequestBody Map<String, String> request) {
        String plainPassword = request.get("password");
        
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Password không được để trống");
            return error;
        }
        
        String encodedPassword = PasswordEncoderUtil.encodePassword(plainPassword);
        boolean matches = PasswordEncoderUtil.matches(plainPassword, encodedPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("plainPassword", plainPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        result.put("message", "Mã hóa thành công");
        
        return result;
    }
    
    @PostMapping("/check-password")
    public Map<String, Object> checkPassword(@RequestBody Map<String, String> request) {
        String plainPassword = request.get("plainPassword");
        String encodedPassword = request.get("encodedPassword");
        
        if (plainPassword == null || encodedPassword == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "plainPassword và encodedPassword không được để trống");
            return error;
        }
        
        boolean matches = PasswordEncoderUtil.matches(plainPassword, encodedPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("plainPassword", plainPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        result.put("message", matches ? "Mật khẩu khớp" : "Mật khẩu không khớp");
        
        return result;
    }
}
