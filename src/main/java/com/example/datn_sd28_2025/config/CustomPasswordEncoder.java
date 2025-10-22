package com.example.datn_sd28_2025.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {
    
    private final DelegatingPasswordEncoder delegatingPasswordEncoder;
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    
    public CustomPasswordEncoder() {
        // Tạo map các password encoder
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", bcryptEncoder);
        encoders.put("noop", new NoOpPasswordEncoder());
        
        // Tạo DelegatingPasswordEncoder với BCrypt làm default
        this.delegatingPasswordEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
    }
    
    @Override
    public String encode(CharSequence rawPassword) {
        // Mã hóa mật khẩu mới bằng BCrypt (default)
        return delegatingPasswordEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Nếu encodedPassword không có prefix, coi như plain text
        if (!hasPasswordPrefix(encodedPassword)) {
            return rawPassword.toString().equals(encodedPassword);
        }
        
        // Sử dụng DelegatingPasswordEncoder để xử lý
        return delegatingPasswordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Kiểm tra xem password có prefix không
     */
    private boolean hasPasswordPrefix(String password) {
        if (password == null || !password.startsWith("{")) {
            return false;
        }
        
        int endIndex = password.indexOf("}");
        return endIndex > 1 && endIndex < password.length() - 1;
    }
    
    /**
     * Custom NoOp Password Encoder cho plain text passwords
     */
    private static class NoOpPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }
        
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}
