package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/auth")
@CrossOrigin(origins = "*")
public class AuthTestController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            response.put("authenticated", auth != null && auth.isAuthenticated());
            response.put("principal", auth != null ? auth.getPrincipal() : null);
            response.put("authorities", auth != null ? auth.getAuthorities() : null);
            response.put("name", auth != null ? auth.getName() : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("timestamp", System.currentTimeMillis());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            response.put("user", auth.getName());
            response.put("authorities", auth.getAuthorities());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<NhanVien> users = nhanVienRepository.findAll();
            response.put("users", users);
            response.put("count", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var user = username.contains("@") 
                ? nhanVienRepository.findByEmail(username)
                : nhanVienRepository.findByTaiKhoan(username);
                
            if (user.isPresent()) {
                NhanVien nhanVien = user.get();
                response.put("found", true);
                response.put("user", Map.of(
                    "id", nhanVien.getId(),
                    "taiKhoan", nhanVien.getTaiKhoan(),
                    "hoTen", nhanVien.getHoTen(),
                    "email", nhanVien.getEmail(),
                    "matKhau", nhanVien.getMatKhau(),
                    "trangThai", nhanVien.getTrangThai(),
                    "chucVu", nhanVien.getChucVu()
                ));
            } else {
                response.put("found", false);
                response.put("message", "User not found");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
