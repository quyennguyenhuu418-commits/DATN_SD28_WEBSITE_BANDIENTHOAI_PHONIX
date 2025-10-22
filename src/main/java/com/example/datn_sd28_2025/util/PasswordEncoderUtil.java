package com.example.datn_sd28_2025.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderUtil {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Mã hóa mật khẩu plain text thành BCrypt
     */
    public static String encodePassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Kiểm tra mật khẩu plain text có khớp với BCrypt hash không
     */
    public static boolean matches(String plainPassword, String encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword);
    }
    
    /**
     * Tạo BCrypt hash cho mật khẩu "123456"
     * Sử dụng để update database
     */
    public static void main(String[] args) {
        String plainPassword = "123456";
        String encodedPassword = encodePassword(plainPassword);
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("BCrypt encoded: " + encodedPassword);
        System.out.println("Matches check: " + matches(plainPassword, encodedPassword));
        
        // Tạo hash cho các mật khẩu phổ biến
        System.out.println("\n=== Common passwords ===");
        String[] passwords = {"123456", "admin", "password", "123456789"};
        for (String pwd : passwords) {
            System.out.println(pwd + " -> " + encodePassword(pwd));
        }
    }
}
